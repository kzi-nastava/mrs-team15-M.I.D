import { Component, EventEmitter, Output, Input, ChangeDetectorRef, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { lastValueFrom } from 'rxjs';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { FormsModule } from '@angular/forms';
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';
import { RidePreferenceForm } from '../ride-preference-form/ride-preference-form';
import { RideService } from '../../../services/ride.service';
import { PassengerService } from '../../../services/passenger.service';
import { MapRouteService } from '../../../services/map-route.service';

// Interface for favorite route data structure
interface FavoriteRoute {
  id?: number | null;
  name: string;
  pickup: string;
  destination: string;
  stops?: string[];
}

// Main form component for ordering rides
// Handles address input, autocomplete suggestions, favorite routes, route estimation and navigation to preferences
@Component({
  selector: 'app-ride-ordering-form',
  standalone: true,
  imports: [Button, InputComponent, CommonModule, FormsModule, RidePreferenceForm],
  templateUrl: './ride-ordering-form.html',
  styleUrls: ['./ride-ordering-form.css'],
})
export class RideOrderingForm implements OnInit {
  // Flag indicating if user already has an active ride
  @Input() hasActiveRide: boolean = false;
  @Input() disabled: boolean = false;
  
  // Pickup address input
  pickupAddress: string = '';
  // Destination address input
  destinationAddress: string = '';
  // Array of intermediate stop addresses
  stops: string[] = [];
  // Index of currently dragged stop (for reordering)
  draggedIndex: number | null = null;
  // Index of stop currently being dragged over
  dragOverIndex: number | null = null;

  // List of user's favorite routes
  favorites: FavoriteRoute[] = [];

  // Currently selected favorite route name
  selectedFavorite: string | null = null;
  // Favorite routes dropdown visibility
  favoriteOpen: boolean = false;

  // Form validator instance
  validator: FromValidator = new FromValidator();
  // Last route estimate from backend
  lastEstimate: any = null;
  // ID of currently selected favorite route
  currentFavoriteRouteId: number | null = null;
  // Selected vehicle type for the ride
  selectedVehicleType: 'STANDARD' | 'LUXURY' | 'VAN' = 'STANDARD';

  // Checks if form has any validation errors
  hasErrors(): boolean {
    if (this.validator.addressError(this.pickupAddress) || this.validator.addressError(this.destinationAddress)) return true;
    for (const s of this.stops) {
      if (this.validator.addressError(s)) return true;
    }
    return false;
  }

  // Applies selected favorite route to form fields
  applyFavorite() {
    const fav = this.favorites.find(f => f.name === this.selectedFavorite);
    if (!fav) return;
    this.pickupAddress = fav.pickup;
    this.destinationAddress = fav.destination;
    this.stops = fav.stops ? [...fav.stops] : [];
  }

  // Toggles favorite routes dropdown visibility
  toggleFavoriteDropdown() {
    this.favoriteOpen = !this.favoriteOpen;
  }

  // TrackBy function for ngFor performance optimization
  trackByIndex(index: number, _item: any) {
    return index;
  }

  // TrackBy function for favorite routes list
  trackByRouteId(index: number, item: FavoriteRoute) {
  return item.id ?? index;
  }

  // Handles favorite route selection from dropdown
  // Fetches full route details and displays on map
  selectFavorite(name: string) {
    if (name === '') {
      this.selectedFavorite = '';
      this.pickupAddress = '';
      this.destinationAddress = '';
      this.stops = [];
      // clear any previously shown estimate when user chooses the placeholder
      this.lastEstimate = null;
      this.currentFavoriteRouteId = null;
    } else {
      this.selectedFavorite = name;
      const fav = this.favorites.find(f => f.name === name);
      if (fav && typeof fav.id === 'number') {
        // fetch full route details from backend for this favorite
        this.passengerService.getFavoriteRoute(fav.id).subscribe({
          next: (r: any) => {
            console.log('Fetched favorite route', r);
            try {
              const distance = r?.distanceKm ?? r?.distance ?? r?.distanceKm;
              const time = r?.estimatedTimeMinutes ?? r?.estimatedDurationMin ?? r?.estimatedTime ?? r?.estimatedDuration;
              console.log('Favorite route estimate', { distanceKm: distance, estimatedTimeMinutes: time });
              this.lastEstimate = r;
              this.currentFavoriteRouteId = fav.id ?? null;

              let roadRoute = r?.route ?? r?.routePoints ?? r?.routeGeometry ?? null;
              if (Array.isArray(roadRoute) && roadRoute.length > 0) {
                const normalized = roadRoute.map((p: any) => ({ lat: p.lat ?? p.latitude ?? p[1], lng: p.lng ?? p.longitude ?? p[0] }));
                this.mapRouteService.drawRoute(normalized);
                
                try {
                  const markers: { lat: number; lng: number; display?: string }[] = [];
                  if (normalized.length > 0) markers.push({ lat: normalized[0].lat, lng: normalized[0].lng, display: this.pickupAddress || r.startAddress || fav.pickup });
                  if (r.stopLatitudes && r.stopLongitudes && Array.isArray(r.stopLatitudes) && Array.isArray(r.stopLongitudes)) { markers.push(...r.stopLatitudes.map((lat: number, i: number) => ({ lat, lng: r.stopLongitudes[i], display: this.stops[i] || undefined }))); }
                  if (normalized.length > 1) markers.push({ lat: normalized[normalized.length - 1].lat, lng: normalized[normalized.length - 1].lng, display: this.destinationAddress || r.endAddress || fav.destination });
                  
                  this.mapRouteService.drawMarkers(markers);
                } catch (e) {
                  console.warn('drawing markers for normalized route failed', e);
                }
              } else if (Array.isArray(r?.routeLattitudes) && Array.isArray(r?.routeLongitudes) && r.routeLattitudes.length === r.routeLongitudes.length) {
                const pts = r.routeLattitudes.map((lat: number, i: number) => ({ lat, lng: r.routeLongitudes[i] }));
                const normalizedPts = pts.map((p: any) => ({ lat: p.lat, lng: p.lng }));
                this.mapRouteService.drawRoute(normalizedPts);
                try {
                  const markers: { lat: number; lng: number; display?: string }[] = [];
                  if (normalizedPts.length > 0) markers.push({ lat: normalizedPts[0].lat, lng: normalizedPts[0].lng, display: this.pickupAddress || r.startAddress || fav.pickup });
                  if (normalizedPts.length > 1) markers.push({ lat: normalizedPts[normalizedPts.length - 1].lat, lng: normalizedPts[normalizedPts.length - 1].lng, display: this.destinationAddress || r.endAddress || fav.destination });
                  this.mapRouteService.drawMarkers(markers);
                } catch (e) { console.warn('drawing markers for routeLattitudes failed', e); }
              } else if (Array.isArray(r?.routeLatitudes) && Array.isArray(r?.routeLongitudes) && r.routeLatitudes.length === r.routeLongitudes.length) {
                const pts = r.routeLatitudes.map((lat: number, i: number) => ({ lat, lng: r.routeLongitudes[i] }));
                const normalizedPts = pts.map((p: any) => ({ lat: p.lat, lng: p.lng }));
                this.mapRouteService.drawRoute(normalizedPts);
                try {
                  const markers: { lat: number; lng: number; display?: string }[] = [];
                  if (normalizedPts.length > 0) markers.push({ lat: normalizedPts[0].lat, lng: normalizedPts[0].lng, display: this.pickupAddress || r.startAddress || fav.pickup });
                  if (normalizedPts.length > 1) markers.push({ lat: normalizedPts[normalizedPts.length - 1].lat, lng: normalizedPts[normalizedPts.length - 1].lng, display: this.destinationAddress || r.endAddress || fav.destination });
                  this.mapRouteService.drawMarkers(markers);
                } catch (e) { console.warn('drawing markers for routeLatitudes failed', e); }
              } else {
                // no geometry provided by backend — fallback to geocode-based markers/route
                try { this.geocodeAndShowMarkers(); } catch (e) { console.warn('geocode fallback for favorite failed', e); }
              }
            } catch (e) {
              console.warn('Processing favorite route response failed', e);
            }
            try {
              this.pickupAddress = r.startAddress ?? r.pickup ?? r.origin ?? fav.pickup ?? '';
              this.destinationAddress = r.endAddress ?? r.destination ?? r.to ?? fav.destination ?? '';
              this.stops = r.stops ?? r.intermediateStops ?? r.stopAddresses ?? fav.stops ?? [];
              try { this.cdr.detectChanges(); } catch(e) {}
            } catch (e) {
              console.warn('Applying favorite route details failed, falling back', e);
              this.applyFavorite();
            }
          },
          error: (err: any) => {
            console.warn('Failed to load favorite route details', err);
            this.applyFavorite();
          }
        });
      } else {
        console.log('Applying local favorite (no id)', fav);
        this.currentFavoriteRouteId = null;
        this.applyFavorite();
      }
    }
    this.favoriteOpen = false;
  }

  // Adds new empty stop to the stops array
  addStop() {
    this.stops.push('');
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
  }

  // Handles drag start event for stop reordering
  onDragStart(event: DragEvent, index: number) {
    this.draggedIndex = index;
    try { event.dataTransfer?.setData('text/plain', String(index)); } catch (e) {}
    if (event.dataTransfer) event.dataTransfer.effectAllowed = 'move';
  }

  // Handles drag over event for visual feedback
  onDragOver(event: DragEvent, index: number) {
    event.preventDefault();
    this.dragOverIndex = index;
    if (event.dataTransfer) event.dataTransfer.dropEffect = 'move';
  }

  // Clears drag over index when leaving drop zone
  onDragLeave(_event: DragEvent, _index: number) {
    this.dragOverIndex = null;
  }

  // Handles drop event, reorders stops array
  onDrop(event: DragEvent, index: number) {
    event.preventDefault();
    const from = this.draggedIndex !== null ? this.draggedIndex : Number(event.dataTransfer?.getData('text/plain'));
    const to = index;
    if (from === to || from == null) {
      this.dragOverIndex = null;
      this.draggedIndex = null;
      return;
    }
    const item = this.stops.splice(from, 1)[0];
    const insertIndex = from < to ? to : to;
    this.stops.splice(insertIndex, 0, item);
    
    this.dragOverIndex = null;
    this.draggedIndex = null;
    
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
  }

  // Cleans up drag state when drag ends
  onDragEnd(_event: DragEvent) {
    this.dragOverIndex = null;
    this.draggedIndex = null;
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
  }

  // Removes stop at specified index
  removeStop(index: number) {
    this.stops.splice(index, 1);
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
  }

  // Timers for debouncing address changes and suggestions
  private updateTimer: any = null;
  private suggestTimer: any = null;

  // Arrays holding address suggestions for autocomplete
  pickupSuggestions: Array<any> = [];
  destinationSuggestions: Array<any> = [];
  stopSuggestions: Array<Array<any>> = [];
  
  // Constants and helpers for suggestion caching
  private readonly MIN_QUERY_LENGTH = 3;
  private readonly SUGGESTION_LIMIT = 5;
  private readonly CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
  private suggestionCache = new Map<string, { suggestions: any[]; timestamp: number }>();
  private lastFetchController: AbortController | null = null;

  // Flag to restrict suggestions to Novi Sad area
  restrictSuggestionsToNoviSad: boolean = true;

  // Handles pickup address input change
  onPickupChange(val: string) {
    this.pickupAddress = val;
    this.fetchSuggestionsDebounced(val, 'pickup');
    
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
    console.debug('onPickupChange', val);
  }

  // Handles destination address input change
  onDestinationChange(val: string) {
    this.destinationAddress = val;
    this.fetchSuggestionsDebounced(val, 'destination');
    
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
    console.debug('onDestinationChange', val);
  }

  // Handles stop address input change
  onStopChange(val: string, index: number) {
    this.stops[index] = val;
    this.fetchSuggestionsDebounced(val, 'stop', index);
    
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
    console.debug('onStopChange', index, val);
  }

  // Updates selected vehicle type
  selectVehicleType(type: 'STANDARD' | 'LUXURY' | 'VAN') {
    this.selectedVehicleType = type;
  }

  // Geocodes addresses and displays markers on map
  private async geocodeAndShowMarkers() {
    try {
      const addresses: string[] = [];
      if (this.pickupAddress && this.pickupAddress.trim()) addresses.push(this.pickupAddress);
      for (const s of this.stops) if (s && s.trim()) addresses.push(s);
      if (this.destinationAddress && this.destinationAddress.trim()) addresses.push(this.destinationAddress);
      console.debug('geocodeAndShowMarkers addresses=', addresses);
      if (addresses.length === 0) return this.mapRouteService.clearAlert();

      const geos = await Promise.all(addresses.map(a => this.rideService.geocodeAddress(a)));
      const points: { lat: number; lng: number }[] = [];
      for (const g of geos) {
        if (g) points.push({ lat: g.lat, lng: g.lon });
      }
      console.debug('geocodeAndShowMarkers points=', points.length);

      if (points.length > 1) {
        
        try {
          const estimateReq = {
            startAddress: this.pickupAddress || '',
            startLatitude: points[0].lat,
            startLongitude: points[0].lng,
            endAddress: this.destinationAddress || '',
            endLatitude: points[points.length - 1].lat,
            endLongitude: points[points.length - 1].lng,
            stopAddresses: this.stops.filter(s => s && s.trim()),
            stopLatitudes: points.slice(1, points.length - 1).map(p => p.lat),
            stopLongitudes: points.slice(1, points.length - 1).map(p => p.lng),
          };

          const resp = await this.rideService.estimateRoute(estimateReq).catch(e => { console.warn('routing call failed', e); return null; });
          const roadRoute = resp?.route ?? resp?.routePoints ?? resp;
          if (Array.isArray(roadRoute) && roadRoute.length > 0) {
            const normalized = roadRoute.map((p: any) => ({ lat: p.lat ?? p.latitude ?? p[1], lng: p.lng ?? p.longitude ?? p[0] }));
            this.mapRouteService.drawRoute(normalized);
            return;
          }
        } catch (e) {
          console.warn('estimateRoute failed', e);
        }
      }

      if (points.length > 0) {
        this.mapRouteService.drawRoute(points.map(p => ({ lat: p.lat, lng: p.lng })));
      }
    } catch (e) {
      console.warn('geocodeAndShowMarkers failed', e);
    }
  }

  // Debounces suggestion fetching to avoid excessive API calls
  private fetchSuggestionsDebounced(query: string, target: 'pickup' | 'destination' | 'stop', index?: number, delay =10) {
    if (this.suggestTimer) clearTimeout(this.suggestTimer);
    this.suggestTimer = setTimeout(() => { this.fetchSuggestions(query, target, index); }, delay);
  }

  // Aborts any ongoing suggestion fetch request
  private abortOngoingFetch(): void {
    if (this.lastFetchController) {
      try { this.lastFetchController.abort(); } catch (e) {}
      this.lastFetchController = null;
    }
  }

  // Updates suggestions array for specified target field
  private setSuggestionsForTarget(target: 'pickup' | 'destination' | 'stop', suggestions: any[], index?: number) {
    if (target === 'pickup') this.pickupSuggestions = suggestions;
    else if (target === 'destination') this.destinationSuggestions = suggestions;
    else if (target === 'stop' && typeof index === 'number') {
      // ensure array length
      while (this.stopSuggestions.length <= index) this.stopSuggestions.push([]);
      this.stopSuggestions[index] = suggestions;
    }
    try { this.cdr.detectChanges(); } catch (e) {}
  }

  // Fetches address suggestions from geocoding API with caching
  private async fetchSuggestions(query: string, target: 'pickup' | 'destination' | 'stop', index?: number) {
    try {
      if (!query || query.trim().length < this.MIN_QUERY_LENGTH) {
        if (target === 'pickup') this.pickupSuggestions = [];
        else if (target === 'destination') this.destinationSuggestions = [];
        else if (target === 'stop' && typeof index === 'number') this.stopSuggestions[index] = [];
        return;
      }

      const normalizedQuery = query.trim().toLowerCase();
      const cached = this.suggestionCache.get(normalizedQuery);
      if (cached && (Date.now() - cached.timestamp) < this.CACHE_DURATION) {
        this.setSuggestionsForTarget(target, cached.suggestions, index);
        return;
      }

      this.abortOngoingFetch();
      this.lastFetchController = new AbortController();

      const onQuick = (items: any[]) => {
        const s = (items || []).slice(0, this.SUGGESTION_LIMIT).map((it: any) => ({ display: it.display, raw: it.raw, lat: it.lat, lng: it.lon }));
        this.setSuggestionsForTarget(target, s, index);
      };

      const onFinal = (items: any[]) => {
        const final = (items || []).slice(0, this.SUGGESTION_LIMIT).map((it: any) => ({ display: it.display, raw: it.raw, lat: it.lat, lng: it.lon }));
        this.suggestionCache.set(normalizedQuery, { suggestions: final, timestamp: Date.now() });
        this.setSuggestionsForTarget(target, final, index);
      };

      try {
        await this.rideService.fetchParallelSuggestions(query, onQuick, onFinal, this.lastFetchController.signal, this.SUGGESTION_LIMIT, true);
      } catch (e: any) {
        if (e && e.name === 'AbortError') return;
        console.warn('Parallel suggestions failed', e);
      }
    } catch (err) {
      console.warn('fetchSuggestions failed', err);
    }
  }

  // Handles selection of pickup address suggestion
  onPickupSuggestion(item: any) {
    if (!item) return;
    const d = item.raw || item;
    const chosen = item.display || d.display_name || d.name || '';
    const normalized = (chosen || '').replace(/\s+/g, ' ').trim();
    this.pickupAddress = normalized;
    this.pickupSuggestions = [];
    
    try { this.onPickupChange(this.pickupAddress); } catch (e) {}
    console.debug('onPickupSuggestion chosen', { item, pickupAddress: this.pickupAddress, len: (this.pickupAddress || '').length, valid: this.validator.addressError(this.pickupAddress) });
  }

  // Handles selection of destination address suggestion
  onDestinationSuggestion(item: any) {
    if (!item) return;
    const d = item.raw || item;
    const chosen = item.display || d.display_name || d.name || '';
    const normalized = (chosen || '').replace(/\s+/g, ' ').trim();
    this.destinationAddress = normalized;
    this.destinationSuggestions = [];
    try { this.onDestinationChange(this.destinationAddress); } catch (e) {}
    console.debug('onDestinationSuggestion chosen', { item, destinationAddress: this.destinationAddress, len: (this.destinationAddress || '').length, valid: this.validator.addressError(this.destinationAddress) });
  }

  // Handles selection of stop address suggestion
  onStopSuggestion(item: any, index: number) {
    if (!item) return;
    const d = item.raw || item;
    const chosen = item.display || d.display_name || d.name || '';
    const normalized = (chosen || '').replace(/\s+/g, ' ').trim();
    this.stops[index] = normalized;
    this.stopSuggestions[index] = [];
    try { this.onStopChange(normalized, index); } catch (e) {}
    console.debug('onStopSuggestion chosen', { index, item, stopValue: this.stops[index], len: (this.stops[index] || '').length, valid: this.validator.addressError(this.stops[index]) });
  }

  // Validates addresses, geocodes them, calls backend for route estimate and displays on map
  async showRoute() {
    const pickupErr = this.validator.addressError(this.pickupAddress);
    const destErr = this.validator.addressError(this.destinationAddress);
    for (let i = 0; i < this.stops.length; i++) {
      if (this.validator.addressError(this.stops[i])) console.debug('stop validation', i, this.stops[i], this.validator.addressError(this.stops[i]));
    }
    console.log('showRoute validation', { pickup: { value: this.pickupAddress, err: pickupErr }, destination: { value: this.destinationAddress, err: destErr } });

    if (this.hasErrors()) {
      console.warn('showRoute detected validation errors, attempting geocode fallback');
      try {
        const gu = await Promise.all([
          this.rideService.geocodeAddress(this.pickupAddress),
          this.rideService.geocodeAddress(this.destinationAddress)
        ]);
        const ok = !!(gu[0] && gu[1]);
        console.log('geocode fallback results', gu);
        if (!ok) {
          console.warn('showRoute aborted: validation failed and geocode fallback did not find both addresses');
          return;
        }
        
      } catch (e) {
        console.warn('geocode fallback error', e);
        return;
      }
    }

    try {
      const startGeo = await this.rideService.geocodeAddress(this.pickupAddress) || { lat: 0, lon: 0 };
      const endGeo = await this.rideService.geocodeAddress(this.destinationAddress) || { lat: 0, lon: 0 };

      const stopAddresses = this.stops.filter(s => s && s.trim().length > 0);
      const stopLatitudes: number[] = [];
      const stopLongitudes: number[] = [];
      for (const s of stopAddresses) {
        const g = await this.rideService.geocodeAddress(s);
        if (g) {
          stopLatitudes.push(g.lat);
          stopLongitudes.push(g.lon);
        }
      }

      const estimateReq = {
        startAddress: this.pickupAddress,
        startLatitude: startGeo.lat,
        startLongitude: startGeo.lon,
        endAddress: this.destinationAddress,
        endLatitude: endGeo.lat,
        endLongitude: endGeo.lon,
        stopAddresses: stopAddresses,
        stopLatitudes: stopLatitudes,
        stopLongitudes: stopLongitudes,
      };

      // calling backend to get distance/time/price estimates
      let routeDrawn = false;
      try {
        console.log('estimateReq ->', estimateReq);
        const estimateResp = await this.rideService.estimateRoute(estimateReq);
        
        if (estimateResp) {
          estimateResp.distanceKm = typeof estimateResp.distanceKm === 'number' ? Math.round(estimateResp.distanceKm * 1000) / 1000 : estimateResp.distanceKm;
          estimateResp.priceEstimate = typeof estimateResp.priceEstimate === 'number' ? Math.round(estimateResp.priceEstimate * 1000) / 1000 : estimateResp.priceEstimate;
          estimateResp.estimatedTimeMinutes = typeof estimateResp.estimatedTimeMinutes === 'number' ? Math.round(estimateResp.estimatedTimeMinutes) : estimateResp.estimatedTimeMinutes;
        }
        this.lastEstimate = estimateResp;
        console.log('Route estimate from backend', estimateResp);
        try { this.cdr.detectChanges(); } catch(e) {}

        let routeDrawn = false;
        try {
          const roadRoute = estimateResp?.route ?? estimateResp?.routePoints ?? null;
          if (Array.isArray(roadRoute) && roadRoute.length > 0) {
            const normalized = roadRoute.map((p: any) => ({ lat: p.lat ?? p.latitude ?? p[1], lng: p.lng ?? p.longitude ?? p[0] }));
            this.mapRouteService.drawRoute(normalized);
            routeDrawn = true;
          } 
        } catch (e) {
          console.warn('Drawing route from estimate response failed', e);
        }
      } catch (e: any) {
        
        console.warn('estimateRoute call failed', e && e.status ? { status: e.status, error: e.error || e.message } : e);
      }

      
      if (!routeDrawn) {
        const points: { lat: number; lng: number; display?: string }[] = [];
        // Only show intermediate stops here — avoid drawing start/end as markers
        for (const s of stopLatitudes.map((lat, i) => ({ lat, lng: stopLongitudes[i], display: stopAddresses[i] }))) {
          points.push(s as any);
        }

        // Draw only the collected stop markers (may be empty)
        this.mapRouteService.drawMarkers(points);
      }
    } catch (err) {
      console.error('Show route failed', err);
    }
  }

  // Flag to show/hide preferences form
  showPreferences: boolean = false;
  // Flag indicating if route has been chosen
  routeChosen: boolean = false;

  // Shows preferences form after route is validated
  chooseRoute() {
    this.showPreferences = true;
    this.routeChosen = true;
    // Keep lastEstimate so the preference form can show the price
    try { this.cdr.detectChanges(); } catch (e) {}
  }

  constructor(private rideService: RideService, private passengerService: PassengerService, private mapRouteService: MapRouteService, private cdr: ChangeDetectorRef, private router: Router) {}
  
  // Formats address display name by truncating to first 3 parts
  private formatDisplayName(raw?: string | null): string {
    if (!raw) return '';
    const parts = raw.split(',').map(p => p.trim()).filter(p => p.length > 0);
    if (parts.length <= 3) return parts.join(', ');
    return parts.slice(0, 3).join(', ');
  }

  ngOnInit(): void {
    this.loadFavoriteRoutes();
    this.mapRouteService.clearRoute();
  }

  // Loads user's favorite routes from backend
  private loadFavoriteRoutes() {
    try {
      console.log('Loading favorite routes from backend');
      this.passengerService.getFavoriteRoutes().subscribe({
        next: (res: any[]) => {
          try {
            console.log('Favorite routes response', res);
            this.favorites = (res || []).map(r => {
              const startRaw = r.startAddress ??  '';
              const endRaw = r.endAddress ?? '';
              const stopsArr: any[] = r.stopAddresses ?? [];
              const hasStartEnd = !!(startRaw && endRaw);

              let name: string;
              if (hasStartEnd) {
                const parts: string[] = [];
                const s = this.formatDisplayName(String(startRaw));
                if (s) parts.push(s);
                if (Array.isArray(stopsArr) && stopsArr.length > 0) {
                  for (const st of stopsArr) {
                    const fs = this.formatDisplayName(String(st ?? ''));
                    if (fs) parts.push(fs);
                  }
                }
                const e = this.formatDisplayName(String(endRaw));
                if (e) parts.push(e);
                name = parts.join(' → ');
              } else {
                name = this.formatDisplayName(r.name ?? r.routeName ?? 'Favorite');
              }

              return {
                id: r.routeId ?? null,
                name,
                pickup: startRaw,
                destination: endRaw,
                stops: Array.isArray(stopsArr) ? stopsArr : []
              };
            });
            console.log('Loaded favorite routes', this.favorites);
            try { this.cdr.detectChanges(); } catch(e) {}
          } catch (mapErr) {
            console.warn('Mapping favorite routes failed', mapErr);
          }
        },
        error: (err) => {
          console.warn('Failed to load favorite routes', err);
        }
      });
    } catch (e) {
      console.warn('loadFavoriteRoutes failed', e);
    }
  }

  // Handles confirmation of ride preferences, builds order DTO and navigates to finding driver page
  async onPreferencesConfirm(prefs: any) {
    console.log('Preferences confirmed from form:', prefs);
    
    // Check if user has active ride
    if (this.hasActiveRide) {
      console.warn('Cannot order ride: user already has an active ride');
      this.orderAttempt.emit({ error: 'You already have an active ride' });
      return;
    }
    
    try {
      const startGeo = await this.rideService.geocodeAddress(this.pickupAddress) || { lat: 0, lon: 0 };
      const endGeo = await this.rideService.geocodeAddress(this.destinationAddress) || { lat: 0, lon: 0 };

      const stopAddresses = this.stops.filter(s => s && s.trim().length > 0);
      const stopLatitudes: number[] = [];
      const stopLongitudes: number[] = [];
      for (const s of stopAddresses) {
        const g = await this.rideService.geocodeAddress(s);
        if (g) {
          stopLatitudes.push(g.lat);
          stopLongitudes.push(g.lon);
        }
      }

      const estimateReq = {
        startAddress: this.pickupAddress,
        startLatitude: startGeo.lat,
        startLongitude: startGeo.lon,
        endAddress: this.destinationAddress,
        endLatitude: endGeo.lat,
        endLongitude: endGeo.lon,
        stopAddresses: stopAddresses,
        stopLatitudes: stopLatitudes,
        stopLongitudes: stopLongitudes,
      };

      const route = await this.rideService.estimateRoute(estimateReq);
      console.log('Route estimate', route);

      
      let normalizedRoute: any[] | null = null;
      try {
        const roadRoute = route?.route ?? route?.routePoints ?? null;
        if (Array.isArray(roadRoute) && roadRoute.length > 0) {
          normalizedRoute = roadRoute.map((p: any) => ({ lat: p.lat ?? p.latitude ?? p[1], lng: p.lng ?? p.longitude ?? p[0], display: p.display || p.name }));
        }
      } catch (e) {
        console.warn('normalizing route failed', e);
        normalizedRoute = null;
      }

      const userJson = localStorage.getItem('user');

      const vehicleTypeChosen = (prefs.vehicleType ? (prefs.vehicleType as string).toUpperCase() : (this.selectedVehicleType || 'STANDARD')) as 'STANDARD' | 'LUXURY' | 'VAN';

      
      let priceForType = route?.priceEstimate ?? 0;
      try {
        if (vehicleTypeChosen === 'STANDARD') priceForType = route?.priceEstimateStandard ?? route?.priceEstimate ?? 0;
        else if (vehicleTypeChosen === 'LUXURY') priceForType = route?.priceEstimateLuxury ?? route?.priceEstimate ?? 0;
        else if (vehicleTypeChosen === 'VAN') priceForType = route?.priceEstimateVan ?? route?.priceEstimate ?? 0;
      } catch (e) { priceForType = route?.priceEstimate ?? 0; }

      
      const favoriteRouteId = this.currentFavoriteRouteId ?? null;

      const orderDto: any = {
        startAddress: estimateReq.startAddress,
        startLatitude: estimateReq.startLatitude,
        startLongitude: estimateReq.startLongitude,
        endAddress: estimateReq.endAddress,
        endLatitude: estimateReq.endLatitude,
        endLongitude: estimateReq.endLongitude,
        stopAddresses: estimateReq.stopAddresses,
        stopLatitudes: estimateReq.stopLatitudes,
        stopLongitudes: estimateReq.stopLongitudes,
        vehicleType: vehicleTypeChosen,
        babyFriendly: !!prefs.babySeat,
        petFriendly: !!prefs.petFriendly,
        linkedPassengers: prefs.guests && prefs.guests.length ? prefs.guests : [],
        scheduledTime: prefs && prefs.scheduledTime ? (prefs.scheduledTime.includes('T') ? prefs.scheduledTime + ':00' : prefs.scheduledTime) : null,
        distanceKm: route?.distanceKm ?? 0,
        estimatedTimeMinutes: route?.estimatedTimeMinutes ?? (route?.estimatedDurationMin ?? 0),
        priceEstimate: priceForType,
        favoriteRouteId: favoriteRouteId,
        route: normalizedRoute,
        routeLattitudes: (function(){
          try {
            const pts = normalizedRoute && normalizedRoute.length > 0 ? normalizedRoute : ([{ lat: startGeo.lat, lng: startGeo.lon }].concat(stopLatitudes.map((lat,i)=>({lat, lng: stopLongitudes[i]}))).concat([{ lat: endGeo.lat, lng: endGeo.lon }]));
            return (pts || []).map((p:any)=>p.lat);
          } catch(e){ return []; }
        })(),
        routeLongitudes: (function(){
          try {
            const pts = normalizedRoute && normalizedRoute.length > 0 ? normalizedRoute : ([{ lat: startGeo.lat, lng: startGeo.lon }].concat(stopLatitudes.map((lat,i)=>({lat, lng: stopLongitudes[i]}))).concat([{ lat: endGeo.lat, lng: endGeo.lon }]));
            return (pts || []).map((p:any)=>p.lng);
          } catch(e){ return []; }
        })(),
      };

      console.log('Order DTO', orderDto);
      
      try {
        await this.router.navigate(['/finding-driver'], { state: { order: orderDto } });
        
        this.orderAttempt.emit({ navigated: true, order: orderDto });
        
        this.showPreferences = false;
      } catch (navErr) {
        console.warn('Navigation to finding-driver failed', navErr);
        
        this.showPreferences = false;
      }

    } catch (err) {
      console.error('Order failed', err);
      this.orderAttempt.emit({ error: err });
    }
  }

  // Event emitted when order attempt is made (success or error)
  @Output() orderAttempt = new EventEmitter<any>();

}
