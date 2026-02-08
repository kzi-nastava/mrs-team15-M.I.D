import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Button } from '../../../shared/components/button/button';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';
import { RideService } from '../../../services/ride.service';
import { Router } from '@angular/router';
import { MapRouteService } from '../../../services/map-route.service';

interface Coordinates {
  lat: number;
  lng: number;
}

interface AddressSuggestion {
  display: string;
  raw: any;
  lat: number;
  lng: number;
}

interface GeocodeResult {
  lat: number;
  lon: number;
}

@Component({
  selector: 'app-ride-estimation-form',
  imports: [Button, InputComponent, CommonModule],
  templateUrl: './ride-estimation-form.html',
  styleUrl: './ride-estimation-form.css',
})
export class RideEstimationForm implements OnInit, OnDestroy {
  pickupAddress = '';
  destinationAddress = '';
  
  pickupCoordinates: Coordinates | null = null;
  destinationCoordinates: Coordinates | null = null;
  
  message = '';
  showMessage = false;
  showEstimationInfo = false;
  estimatedDistanceKm?: number;
  estimatedDurationMin?: number;
  isLoading = false;

  pickupSuggestions: AddressSuggestion[] = [];
  destinationSuggestions: AddressSuggestion[] = [];
  
  private readonly NOVI_SAD_BOUNDS = {
    minLat: 45.2,
    maxLat: 45.3,
    minLng: 19.7,
    maxLng: 19.95
  };
  private readonly DEBOUNCE_DELAY = 400;
  private readonly MIN_QUERY_LENGTH = 3;
  private readonly SUGGESTION_LIMIT = 5;
  private readonly CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
  
  private suggestTimer: any = null;
  private suggestionCache = new Map<string, { suggestions: AddressSuggestion[], timestamp: number }>();
  private lastFetchController: AbortController | null = null;
  validator: FromValidator = new FromValidator();

  constructor(
    private cdr: ChangeDetectorRef,
    private rideService: RideService,
    private mapRouteService: MapRouteService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.mapRouteService.clearRoute();
  }

  ngOnDestroy(): void {
    this.clearSuggestTimer();
    this.abortOngoingFetch();
  }

  hasErrors(): boolean {
    return !!(
      this.validator.addressError(this.pickupAddress) ||
      this.validator.addressError(this.destinationAddress)
    );
  }

  showMessageToast(message: string): void {
    this.message = message;
    this.showMessage = true;
    this.cdr.detectChanges();
    setTimeout(() => {
      this.showMessage = false;
      this.cdr.detectChanges();
    }, 3000);
  }

  trackByIndex(index: number, _item: any): number {
    return index;
  }

  onPickupChange(val: string): void {
    this.pickupAddress = val;
    this.pickupCoordinates = null;
    this.pickupSuggestions = [];
    this.fetchSuggestionsDebounced(val, 'pickup');
  }

  onDestinationChange(val: string): void {
    this.destinationAddress = val;
    this.destinationCoordinates = null;
    this.destinationSuggestions = [];
    this.fetchSuggestionsDebounced(val, 'destination');
  }

  onPickupSuggestion(item: AddressSuggestion): void {
    if (!item) return;
    
    this.pickupAddress = item.display;
    this.pickupCoordinates = { lat: item.lat, lng: item.lng };
    this.pickupSuggestions = [];
    this.clearSuggestTimer();
    this.abortOngoingFetch();
    this.cdr.detectChanges();
  }

  onDestinationSuggestion(item: AddressSuggestion): void {
    if (!item) return;
    
    this.destinationAddress = item.display;
    this.destinationCoordinates = { lat: item.lat, lng: item.lng };
    this.destinationSuggestions = [];
    this.clearSuggestTimer();
    this.abortOngoingFetch();
    this.cdr.detectChanges();
  }

  
  private fetchSuggestionsDebounced(
    query: string,
    target: 'pickup' | 'destination'
  ): void {
    this.clearSuggestTimer();
    this.abortOngoingFetch();
    
    this.suggestTimer = setTimeout(() => {
      this.fetchSuggestions(query, target);
    }, this.DEBOUNCE_DELAY);
  }

  private clearSuggestTimer(): void {
    if (this.suggestTimer) {
      clearTimeout(this.suggestTimer);
      this.suggestTimer = null;
    }
  }

  private abortOngoingFetch(): void {
    if (this.lastFetchController) {
      this.lastFetchController.abort();
      this.lastFetchController = null;
    }
  }

  private async fetchSuggestions(
    query: string,
    target: 'pickup' | 'destination'
  ): Promise<void> {
    try {
      if (!query || query.trim().length < this.MIN_QUERY_LENGTH) {
        this.clearSuggestions(target);
        return;
      }

      const normalizedQuery = query.trim().toLowerCase();
      const cached = this.suggestionCache.get(normalizedQuery);
      if (cached && (Date.now() - cached.timestamp) < this.CACHE_DURATION) {
        if (target === 'pickup') this.pickupSuggestions = cached.suggestions;
        else this.destinationSuggestions = cached.suggestions;
        this.cdr.detectChanges();
        return;
      }

      this.abortOngoingFetch();
      this.lastFetchController = new AbortController();

      let finalSuggestions: AddressSuggestion[] = [];

      const onQuick = (items: any[]) => {
        const s = (items || []).slice(0, this.SUGGESTION_LIMIT).map((it: any) => ({ display: it.display, raw: it.raw, lat: it.lat, lng: it.lon }));
        if (target === 'pickup') this.pickupSuggestions = s;
        else this.destinationSuggestions = s;
        this.cdr.detectChanges();
      };

      const onFinal = (items: any[]) => {
        finalSuggestions = (items || []).slice(0, this.SUGGESTION_LIMIT).map((it: any) => ({ display: it.display, raw: it.raw, lat: it.lat, lng: it.lon }));
        this.suggestionCache.set(normalizedQuery, { suggestions: finalSuggestions, timestamp: Date.now() });
        if (target === 'pickup') this.pickupSuggestions = finalSuggestions;
        else this.destinationSuggestions = finalSuggestions;
        this.cdr.detectChanges();
      };

      try {
        await this.rideService.fetchParallelSuggestions(query, onQuick, onFinal, this.lastFetchController.signal, this.SUGGESTION_LIMIT, true);
      } catch (e: any) {
        if (e && e.name === 'AbortError') return;
        console.warn('Parallel suggestions failed', e);
      }
    } catch (error: any) {
      if (error && error.name === 'AbortError') return;
      console.error('Failed to fetch suggestions:', error);
      this.clearSuggestions(target);
    }
  }

  private isInNoviSadArea(lat: number, lng: number): boolean {
    return (
      lat >= this.NOVI_SAD_BOUNDS.minLat &&
      lat <= this.NOVI_SAD_BOUNDS.maxLat &&
      lng >= this.NOVI_SAD_BOUNDS.minLng &&
      lng <= this.NOVI_SAD_BOUNDS.maxLng
    );
  }

  private formatAddressDisplay(data: any): string {
    const address = data.address;
    
    if (!address) {
      return data.display_name || '';
    }

    const parts: string[] = [];
    
    if (address.road) {
      let street = address.road;
      if (address.house_number) {
        street += ' ' + address.house_number;
      }
      parts.push(street);
    }
    
    const city = address.city || address.town || 'Novi Sad';
    if (address.suburb && address.suburb !== city) {
      parts.push(address.suburb);
    }
    
    if (parts.length === 0 && address.suburb) {
      parts.push(address.suburb);
    }
    parts.push(city);
    
    return parts.join(', ');
  }

  private clearSuggestions(target: 'pickup' | 'destination'): void {
    if (target === 'pickup') {
      this.pickupSuggestions = [];
    } else {
      this.destinationSuggestions = [];
    }
    this.cdr.detectChanges();
  }

  async estimateRide(): Promise<void> {
    if (this.hasErrors() || this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.cdr.detectChanges();

    try {
      const coordinates = await this.resolveCoordinates();
      if (!coordinates) {
        return; 
      }

      await this.requestEstimation(coordinates);
    } catch (error) {
      console.error('Estimation failed:', error);
      this.showMessageToast('Failed to estimate ride. Please try again.');
    } finally {
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  private async resolveCoordinates(): Promise<{
    startLat: number;
    startLng: number;
    endLat: number;
    endLng: number;
  } | null> {
    let startLat: number, startLng: number, endLat: number, endLng: number;

    if (this.pickupCoordinates) {
      startLat = this.pickupCoordinates.lat;
      startLng = this.pickupCoordinates.lng;
    } else {
      const pickupGeo = await this.geocodeAddress(this.pickupAddress);
      if (!pickupGeo) {
        this.showMessageToast('Could not find pickup address');
        return null;
      }
      startLat = pickupGeo.lat;
      startLng = pickupGeo.lon;
    }

    if (this.destinationCoordinates) {
      endLat = this.destinationCoordinates.lat;
      endLng = this.destinationCoordinates.lng;
    } else {
      const destGeo = await this.geocodeAddress(this.destinationAddress);
      if (!destGeo) {
        this.showMessageToast('Could not find destination address');
        return null;
      }
      endLat = destGeo.lat;
      endLng = destGeo.lon;
    }

    if (!this.isInNoviSadArea(startLat, startLng)) {
      this.showMessageToast('Pickup address must be in Novi Sad');
      return null;
    }

    if (!this.isInNoviSadArea(endLat, endLng)) {
      this.showMessageToast('Destination address must be in Novi Sad');
      return null;
    }

    return { startLat, startLng, endLat, endLng };
  }

  private async geocodeAddress(address: string): Promise<GeocodeResult | null> {
    try {
      return await this.rideService.geocodeAddress(address);
    } catch (error) {
      console.error('Geocoding failed:', error);
      return null;
    }
  }

  private async requestEstimation(coordinates: {
    startLat: number;
    startLng: number;
    endLat: number;
    endLng: number;
  }): Promise<void> {
    const data = {
      startAddress: this.pickupAddress,
      startLatitude: coordinates.startLat,
      startLongitude: coordinates.startLng,
      endAddress: this.destinationAddress,
      endLatitude: coordinates.endLat,
      endLongitude: coordinates.endLng
    };

    return new Promise((resolve, reject) => {
      this.rideService.estimate(data).subscribe({
        next: (response) => {
          this.showEstimationInfo = true;
          this.estimatedDistanceKm = response.distanceKm;
          this.estimatedDurationMin = response.estimatedDurationMin;
          this.mapRouteService.drawRoute(response.route);
          this.cdr.detectChanges();
          resolve();
        },
        error: (err) => {
          const message = typeof err.error === 'string' 
            ? err.error 
            : 'Ride estimation failed. Please try again.';
          this.showMessageToast(message);
          reject(err);
        }
      });
    });
  }
}