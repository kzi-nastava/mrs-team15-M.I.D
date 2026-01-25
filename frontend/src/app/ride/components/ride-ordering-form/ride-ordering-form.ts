import { Component, EventEmitter, Output, ChangeDetectorRef, OnInit } from '@angular/core';
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

interface FavoriteRoute {
  id?: number | null;
  name: string;
  pickup: string;
  destination: string;
  stops?: string[];
}

@Component({
  selector: 'app-ride-ordering-form',
  standalone: true,
  imports: [Button, InputComponent, CommonModule, FormsModule, RidePreferenceForm],
  templateUrl: './ride-ordering-form.html',
  styleUrls: ['./ride-ordering-form.css'],
})
export class RideOrderingForm implements OnInit {
  pickupAddress: string = '';
  destinationAddress: string = '';
  stops: string[] = [];
  draggedIndex: number | null = null;
  dragOverIndex: number | null = null;

  favorites: FavoriteRoute[] = [
    { id: null, name: 'Home → Work', pickup: '123 Home St', destination: '456 Work Ave', stops: [] },
    { id: null, name: 'Airport Ride', pickup: 'Home Address', destination: 'Airport Terminal 1', stops: [] },
  ];
  selectedFavorite: string | null = null;
  favoriteOpen: boolean = false;

  validator: FromValidator = new FromValidator();
  // Last estimate response from backend (distance, time, price)
  lastEstimate: any = null;
  // currently selected favorite route id (if applied from backend)
  currentFavoriteRouteId: number | null = null;
  // Selected vehicle type for pricing and ordering (default STANDARD)
  selectedVehicleType: 'STANDARD' | 'LUXURY' | 'VAN' = 'STANDARD';
  // Last estimate response from backend (distance, time, price)

  hasErrors(): boolean {
    if (this.validator.addressError(this.pickupAddress) || this.validator.addressError(this.destinationAddress)) return true;
    for (const s of this.stops) {
      if (this.validator.addressError(s)) return true;
    }
    return false;
  }

  applyFavorite() {
    const fav = this.favorites.find(f => f.name === this.selectedFavorite);
    if (!fav) return;
    this.pickupAddress = fav.pickup;
    this.destinationAddress = fav.destination;
    this.stops = fav.stops ? [...fav.stops] : [];
  }

  toggleFavoriteDropdown() {
    this.favoriteOpen = !this.favoriteOpen;
  }

  trackByIndex(index: number, _item: any) {
    return index;
  }

  selectFavorite(name: string) {
    if (name === '') {
      this.selectedFavorite = '';
      this.pickupAddress = '';
      this.destinationAddress = '';
      this.stops = [];
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
              // store last estimate so UI can show values similarly to manual estimates
              this.lastEstimate = r;
              // mark current favorite route id as applied
              this.currentFavoriteRouteId = fav.id ?? null;

              // Try to draw returned geometry if present
              let roadRoute = r?.route ?? r?.routePoints ?? r?.routeGeometry ?? null;
              if (Array.isArray(roadRoute) && roadRoute.length > 0) {
                const normalized = roadRoute.map((p: any) => ({ lat: p.lat ?? p.latitude ?? p[1], lng: p.lng ?? p.longitude ?? p[0] }));
                this.mapRouteService.drawRoute(normalized);
                // draw markers for start and end (and optional stops if available)
                try {
                  const markers: { lat: number; lng: number; display?: string }[] = [];
                  if (normalized.length > 0) markers.push({ lat: normalized[0].lat, lng: normalized[0].lng, display: this.pickupAddress || r.startAddress || fav.pickup });
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

  addStop() {
    this.stops.push('');
    // modifying stops invalidates previous estimate
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
  }

  onDragStart(event: DragEvent, index: number) {
    this.draggedIndex = index;
    try { event.dataTransfer?.setData('text/plain', String(index)); } catch (e) {}
    if (event.dataTransfer) event.dataTransfer.effectAllowed = 'move';
  }

  onDragOver(event: DragEvent, index: number) {
    event.preventDefault();
    this.dragOverIndex = index;
    if (event.dataTransfer) event.dataTransfer.dropEffect = 'move';
  }

  onDragLeave(_event: DragEvent, _index: number) {
    this.dragOverIndex = null;
  }

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
    // If dragging from a position before the drop target and we removed it, the target index shifts down by 1
    const insertIndex = from < to ? to : to;
    this.stops.splice(insertIndex, 0, item);
    this.dragOverIndex = null;
    this.draggedIndex = null;
    // reordering stops invalidates previous estimate
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
  }

  onDragEnd(_event: DragEvent) {
    this.dragOverIndex = null;
    this.draggedIndex = null;
    // ensure estimate is cleared if drag changed order
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
  }

  removeStop(index: number) {
    this.stops.splice(index, 1);
    // removing a stop invalidates previous estimate
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
  }

  // -- Address change handlers & map update --
  private updateTimer: any = null;
  private suggestTimer: any = null;

  pickupSuggestions: Array<any> = [];
  destinationSuggestions: Array<any> = [];
  stopSuggestions: Array<Array<any>> = [];
  // If true, suggestions will be restricted/bias to Novi Sad, Serbia
  restrictSuggestionsToNoviSad: boolean = true;

  onPickupChange(val: string) {
    this.pickupAddress = val;
    this.fetchSuggestionsDebounced(val, 'pickup');
    // Clear any previous estimate when inputs change
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
    console.debug('onPickupChange', val);
  }

  onDestinationChange(val: string) {
    this.destinationAddress = val;
    this.fetchSuggestionsDebounced(val, 'destination');
    // Clear any previous estimate when inputs change
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
    console.debug('onDestinationChange', val);
  }

  onStopChange(val: string, index: number) {
    this.stops[index] = val;
    this.fetchSuggestionsDebounced(val, 'stop', index);
    // Clear any previous estimate when inputs change
    this.lastEstimate = null;
    this.currentFavoriteRouteId = null;
    console.debug('onStopChange', index, val);
  }

  selectVehicleType(type: 'STANDARD' | 'LUXURY' | 'VAN') {
    this.selectedVehicleType = type;
  }

  private updateMapMarkersDebounced(delay = 400) {
    if (this.updateTimer) clearTimeout(this.updateTimer);
    this.updateTimer = setTimeout(() => { this.geocodeAndShowMarkers(); }, delay);
  }

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
        // Try backend routing to get road-following polyline
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

          // fallback: if POST /estimate-route did not include geometry, try GET /estimate which returns route points
          try {
            const getResp = await lastValueFrom(this.rideService.estimate({ startAddress: this.pickupAddress || '', destinationAddress: this.destinationAddress || '' }).pipe());
            const getRoute = getResp?.route ?? getResp;
            if (Array.isArray(getRoute) && getRoute.length > 0) {
              const normalized2 = getRoute.map((p: any) => ({ lat: p.lat ?? p.latitude ?? p[1], lng: p.lng ?? p.longitude ?? p[0] }));
              this.mapRouteService.drawRoute(normalized2);
              return;
            }
          } catch (ge) {
            console.warn('fallback GET /estimate failed', ge);
          }
        } catch (e) {
          console.warn('estimateRoute failed', e);
        }
      }

      // fallback: draw straight-line connections between geocoded points
      if (points.length > 0) {
        this.mapRouteService.drawRoute(points.map(p => ({ lat: p.lat, lng: p.lng })));
      }
    } catch (e) {
      console.warn('geocodeAndShowMarkers failed', e);
    }
  }

  // --- Suggestions (Nominatim) ---
  private fetchSuggestionsDebounced(query: string, target: 'pickup' | 'destination' | 'stop', index?: number, delay = 250) {
    if (this.suggestTimer) clearTimeout(this.suggestTimer);
    this.suggestTimer = setTimeout(() => { this.fetchSuggestions(query, target, index); }, delay);
  }

  private async fetchSuggestions(query: string, target: 'pickup' | 'destination' | 'stop', index?: number) {
    try {
      if (!query || query.trim().length < 2) {
        if (target === 'pickup') this.pickupSuggestions = [];
        else if (target === 'destination') this.destinationSuggestions = [];
        else if (target === 'stop' && typeof index === 'number') this.stopSuggestions[index] = [];
        return;
      }

      // bias results to Novi Sad (Serbia). If user typed a house number, use structured query
      const hasNumber = /\d/.test(query);
      let url: string;
      if (this.restrictSuggestionsToNoviSad && hasNumber) {
        // use structured param so house numbers are matched better
        url = `https://nominatim.openstreetmap.org/search?format=json&street=${encodeURIComponent(query)}&city=Novi%20Sad&countrycodes=rs&addressdetails=1&limit=10`;
      } else {
        let q = query;
        if (this.restrictSuggestionsToNoviSad && !/novi\s*sad/i.test(query)) q = `${query} Novi Sad`;
        url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(q)}&addressdetails=1&limit=10&countrycodes=rs`;
      }
      console.debug('fetchSuggestions ->', url);
      const res = await fetch(url, { headers: { 'Accept-Language': 'en' } });
      const data = await res.json();
      console.debug('fetchSuggestions response length=', Array.isArray(data) ? data.length : 0);
      const items = Array.isArray(data) ? data.map((d: any) => ({ display: d.display_name, raw: d })) : [];

      if (target === 'pickup') this.pickupSuggestions = items;
      else if (target === 'destination') this.destinationSuggestions = items;
      else if (target === 'stop' && typeof index === 'number') {
        this.stopSuggestions[index] = items;
      }
    } catch (e) {
      console.warn('fetchSuggestions failed', e);
    }
  }

  onPickupSuggestion(item: any) {
    if (!item) return;
    const d = item.raw || item;
    const chosen = item.display || d.display_name || d.name || '';
    const normalized = (chosen || '').replace(/\s+/g, ' ').trim();
    this.pickupAddress = normalized;
    this.pickupSuggestions = [];
    // ensure change handlers run and validation updates
    try { this.onPickupChange(this.pickupAddress); } catch (e) {}
    console.debug('onPickupSuggestion chosen', { item, pickupAddress: this.pickupAddress, len: (this.pickupAddress || '').length, valid: this.validator.addressError(this.pickupAddress) });
  }

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
        // continue despite validation errors because geocoding succeeded
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

      // Call backend to get distance/time/price estimates
      let routeDrawn = false;
      try {
        console.log('estimateReq ->', estimateReq);
        const estimateResp = await this.rideService.estimateRoute(estimateReq);
        // Round numeric fields to 3 decimals where appropriate
        if (estimateResp) {
          estimateResp.distanceKm = typeof estimateResp.distanceKm === 'number' ? Math.round(estimateResp.distanceKm * 1000) / 1000 : estimateResp.distanceKm;
          estimateResp.priceEstimate = typeof estimateResp.priceEstimate === 'number' ? Math.round(estimateResp.priceEstimate * 1000) / 1000 : estimateResp.priceEstimate;
          estimateResp.estimatedTimeMinutes = typeof estimateResp.estimatedTimeMinutes === 'number' ? Math.round(estimateResp.estimatedTimeMinutes) : estimateResp.estimatedTimeMinutes;
        }
        this.lastEstimate = estimateResp;
        console.log('Route estimate from backend', estimateResp);
        try { this.cdr.detectChanges(); } catch(e) {}

        // Try to draw route geometry returned by estimate-route (POST). If absent, fallback to GET /estimate, then to markers.
        let routeDrawn = false;
        try {
          const roadRoute = estimateResp?.route ?? estimateResp?.routePoints ?? null;
          if (Array.isArray(roadRoute) && roadRoute.length > 0) {
            const normalized = roadRoute.map((p: any) => ({ lat: p.lat ?? p.latitude ?? p[1], lng: p.lng ?? p.longitude ?? p[0] }));
            this.mapRouteService.drawRoute(normalized);
            routeDrawn = true;
          } else {
            // fallback: request geometry via GET /estimate
            try {
              const getResp = await lastValueFrom(this.rideService.estimate({ startAddress: estimateReq.startAddress, destinationAddress: estimateReq.endAddress }).pipe());
              const getRoute = getResp?.route ?? getResp;
              if (Array.isArray(getRoute) && getRoute.length > 0) {
                const normalized2 = getRoute.map((p: any) => ({ lat: p.lat ?? p.latitude ?? p[1], lng: p.lng ?? p.longitude ?? p[0] }));
                this.mapRouteService.drawRoute(normalized2);
                routeDrawn = true;
              }
            } catch (ge) {
              console.warn('fallback GET /estimate failed', ge);
            }
          }
        } catch (e) {
          console.warn('Drawing route from estimate response failed', e);
        }
      } catch (e: any) {
        // Log detailed HTTP error info when available
        console.warn('estimateRoute call failed', e && e.status ? { status: e.status, error: e.error || e.message } : e);
      }

      // Show only markers for start/stops/end (no connecting polyline) if no route was drawn
      if (!routeDrawn) {
        const points: { lat: number; lng: number; display?: string }[] = [];
        if (startGeo) points.push({ lat: startGeo.lat, lng: startGeo.lon, display: this.pickupAddress });
        for (const s of stopLatitudes.map((lat, i) => ({ lat, lng: stopLongitudes[i], display: stopAddresses[i] }))) {
          points.push(s as any);
        }
        if (endGeo) points.push({ lat: endGeo.lat, lng: endGeo.lon, display: this.destinationAddress });

        this.mapRouteService.drawMarkers(points);
      }
    } catch (err) {
      console.error('Show route failed', err);
    }
  }
  

  showPreferences: boolean = false;
  // Whether the user clicked "Choose route" (enables showing Calculate Price)
  routeChosen: boolean = false;

  chooseRoute() {
    this.showPreferences = true;
    this.routeChosen = true;
  }

  constructor(private rideService: RideService, private passengerService: PassengerService, private mapRouteService: MapRouteService, private cdr: ChangeDetectorRef, private router: Router) {}
  private formatDisplayName(raw?: string | null): string {
    if (!raw) return '';
    const parts = raw.split(',').map(p => p.trim()).filter(p => p.length > 0);
    if (parts.length <= 3) return parts.join(', ');
    return parts.slice(0, 3).join(', ');
  }

  ngOnInit(): void {
    this.loadFavoriteRoutes();
  }

  private loadFavoriteRoutes() {
    try {
      // Load favorite routes for authenticated passenger
      this.passengerService.getFavoriteRoutes().subscribe({
        next: (res: any[]) => {
          try {
            this.favorites = (res || []).map(r => {
              const startRaw = r.startAddress ?? r.pickup ?? r.origin ?? '';
              const endRaw = r.endAddress ?? r.destination ?? r.to ?? '';
              const stopsArr: any[] = r.stops ?? r.intermediateStops ?? r.stopAddresses ?? [];
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

  async onPreferencesConfirm(prefs: any) {
    console.log('Preferences confirmed from form:', prefs);
    this.showPreferences = false;

    // Build basic estimate payload by geocoding addresses
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

      // Normalize any returned route geometry so it can be passed to next page
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

      // choose price field based on vehicle type (backend may return per-type fields)
      let priceForType = route?.priceEstimate ?? 0;
      try {
        if (vehicleTypeChosen === 'STANDARD') priceForType = route?.priceEstimateStandard ?? route?.priceEstimate ?? 0;
        else if (vehicleTypeChosen === 'LUXURY') priceForType = route?.priceEstimateLuxury ?? route?.priceEstimate ?? 0;
        else if (vehicleTypeChosen === 'VAN') priceForType = route?.priceEstimateVan ?? route?.priceEstimate ?? 0;
      } catch (e) { priceForType = route?.priceEstimate ?? 0; }

      // prefer the currently applied favorite id (cleared when inputs change)
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
        scheduledTime: null,
        distanceKm: route?.distanceKm ?? 0,
        estimatedTimeMinutes: route?.estimatedTimeMinutes ?? (route?.estimatedDurationMin ?? 0),
        priceEstimate: priceForType,
        favoriteRouteId: favoriteRouteId,
        // include route geometry (if available) so finding-driver can draw it immediately
        route: normalizedRoute,
        // backend expects separate arrays of latitudes/longitudes for the whole polyline
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
      // Navigate to finding-driver immediately with the order payload
      try {
        this.router.navigate(['/finding-driver'], { state: { order: orderDto } });
      } catch (navErr) {
        console.warn('Navigation to finding-driver failed', navErr);
      }

      // Do not call backend here; finding-driver page will call the backend after navigation
      // Emit immediate navigation event so parent can react if needed
      this.orderAttempt.emit({ navigated: true, order: orderDto });

    } catch (err) {
      console.error('Order failed', err);
      this.orderAttempt.emit({ error: err });
    }
  }

  @Output() orderAttempt = new EventEmitter<any>();

}
