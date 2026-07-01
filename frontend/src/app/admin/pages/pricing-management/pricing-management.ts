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

  loadPriceConfigurations(): void {
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    // Call the AdminService to get price configurations, handles success and error responses,
    // updates the priceConfigs and originalPriceConfigs arrays, manages loading state,
    // and triggers change detection to update the UI with the new data or error message.
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

  hasChanges(): boolean {
    const hasChanges = JSON.stringify(this.priceConfigs) !== JSON.stringify(this.originalPriceConfigs);
    return hasChanges;
  }

  saveChanges(): void {

    this.saving = true;
    this.error = null;
    this.success = null;
    this.cdr.detectChanges();

    const updateData = {
      prices: this.priceConfigs
    };


    // Call the AdminService to update price configurations, handles success and error responses,
    // updates the originalPriceConfigs on success, manages saving state,
    // and triggers change detection to update the UI with success or error messages.
    this.adminService.updatePriceConfigurations(updateData).subscribe({
      next: (response) => {
        console.log('Update successful, response:', response);
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
    return isValid;
  }

  onPriceChange(field: 'basePrice' | 'pricePerKm', value: number, config: PriceConfig): void {
    this.cdr.detectChanges();
  }

  // Method to validate the price input and update the corresponding field in the price configuration if valid,
  // otherwise sets an error message. Also logs the validation process and results for debugging purposes.
  validateAndUpdatePrice(config: PriceConfig, field: 'basePrice' | 'pricePerKm', value: number): void {
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
