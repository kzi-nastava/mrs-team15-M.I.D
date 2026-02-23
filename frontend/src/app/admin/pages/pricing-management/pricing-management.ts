import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { Button } from '../../../shared/components/button/button';

interface PriceConfig {
  vehicleType: string;
  basePrice: number;
  pricePerKm: number;
}

interface PriceConfigResponse {
  prices: PriceConfig[];
}

@Component({
  selector: 'app-pricing-management',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent, Button],
  templateUrl: './pricing-management.html',
  styleUrl: './pricing-management.css',
})
export class PricingManagement implements OnInit {
  priceConfigs: PriceConfig[] = [];
  originalPriceConfigs: PriceConfig[] = [];
  loading = false;
  saving = false;
  error: string | null = null;
  success: string | null = null;

  constructor(private adminService: AdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadPriceConfigurations();
  }

  // Method to load price configurations from the backend using the AdminService
  loadPriceConfigurations(): void {
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    this.adminService.getPriceConfigurations().subscribe({
      next: (response: PriceConfigResponse) => {
        this.priceConfigs = [...response.prices];
        this.originalPriceConfigs = [...response.prices];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading price configurations:', error);
        this.error = 'Failed to load price configurations. Please try again.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Method to check if there are any changes to the price configurations compared to the original values
  hasChanges(): boolean {
    const hasChanges = JSON.stringify(this.priceConfigs) !== JSON.stringify(this.originalPriceConfigs);
    return hasChanges;
  }

  // Method to save changes to the price configurations by sending an update request to the backend using the AdminService
  saveChanges(): void {
    this.saving = true;
    this.error = null;
    this.success = null;
    this.cdr.detectChanges();

    const updateData = {
      prices: this.priceConfigs
    };

    // Call the admin service to update price configurations
    this.adminService.updatePriceConfigurations(updateData).subscribe({
      next: (response) => {
        this.originalPriceConfigs = [...this.priceConfigs];
        this.success = 'Price configurations updated successfully!';
        this.saving = false;
        this.cdr.detectChanges();

        // Clear success message after 3 seconds
        setTimeout(() => {
          this.success = null;
          this.cdr.detectChanges();
        }, 3000);
      },
      error: (error) => {
        console.error('Error updating price configurations:', error);
        this.error = 'Failed to update price configurations. Please try again.';
        this.saving = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Helper method to get a user-friendly display name for the vehicle type
  getVehicleTypeDisplayName(vehicleType: string): string {
    switch (vehicleType) {
      case 'STANDARD':
        return 'Standard Vehicle';
      case 'LUXURY':
        return 'Luxury Vehicle';
      case 'VAN':
        return 'Van';
      default:
        return vehicleType;
    }
  }

  // Validation methods
  isValidPrice(price: number): boolean {
    const isValid = price >= 0 && price <= 10000;
    console.log('isValidPrice():', price, 'isValid:', isValid);
    return isValid;
  }

  // Method called when a price input field value changes, logs the change and triggers change detection to update the UI
  onPriceChange(field: 'basePrice' | 'pricePerKm', value: number, config: PriceConfig): void {
    console.log('onPriceChange():', config.vehicleType, field, 'new value:', value);
    console.log('Updated config:', config);
    this.cdr.detectChanges();
  }

  // Method to validate the new price value and update the corresponding field in the price configuration if valid, otherwise sets an error message
  validateAndUpdatePrice(config: PriceConfig, field: 'basePrice' | 'pricePerKm', value: number): void {
    console.log('validateAndUpdatePrice():', config.vehicleType, field, value);
    if (this.isValidPrice(value)) {
      config[field] = value;
      this.error = null;
      console.log('Price updated successfully for', config.vehicleType, field, ':', value);
    } else {
      this.error = 'Price must be between 0 and 10,000';
      console.log('Price validation failed for', config.vehicleType, field, ':', value);
    }
  }
}
