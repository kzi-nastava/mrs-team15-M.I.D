import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Button } from '../../../shared/components/button/button';

export interface AppointedRide {
  id: number;
  route: string;
  passengers: string[];
  date: string;
  time: string;
  estimatedDuration: string;
  status: 'scheduled' | 'in-progress' | 'arriving';
}

@Component({
  selector: 'app-driver-appointed-rides',
  standalone: true,
  imports: [CommonModule, Button],
  templateUrl: './driver-appointed-rides.html',
  styleUrl: './driver-appointed-rides.css',
})
export class DriverAppointedRides {
  appointedRides: AppointedRide[] = [
    {
      id: 1,
      route: 'Bulevar oslobođenja, Novi Sad → Aerodrom Nikola Tesla, Beograd',
      passengers: ['Marko Marković', 'Ana Jovanović'],
      date: '2026-01-08',
      time: '14:30',
      estimatedDuration: '25 min',
      status: 'scheduled'
    },
    {
      id: 2,
      route: 'Trg slobode, Novi Sad → Železnička stanica, Novi Sad',
      passengers: ['Petar Petrović'],
      date: '2026-01-08',
      time: '16:00',
      estimatedDuration: '12 min',
      status: 'in-progress'
    },
    {
      id: 3,
      route: 'Liman 3 → Promenada Shopping',
      passengers: ['Jovana Nikolić', 'Stefan Stojanović', 'Milica Đorđević'],
      date: '2026-01-08',
      time: '18:30',
      estimatedDuration: '18 min',
      status: 'arriving'
    },
    {
      id: 4,
      route: 'Telep → BIG Fashion Novi Sad',
      passengers: ['Nemanja Savić'],
      date: '2026-01-09',
      time: '10:00',
      estimatedDuration: '15 min',
      status: 'scheduled'
    },
    {
      id: 5,
      route: 'Cara Dušana → Petrovaradin Fortress',
      passengers: ['Jelena Matić', 'Nikola Jović'],
      date: '2026-01-09',
      time: '12:30',
      estimatedDuration: '20 min',
      status: 'scheduled'
    }
  ];

  constructor(private router: Router) {}

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    return `${day}.${month}.${year}`;
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'scheduled':
        return 'bg-light text-dark';
      case 'in-progress':
        return 'bg-success';
      case 'arriving':
        return 'bg-warning';
      default:
        return 'bg-secondary';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'scheduled':
        return 'Scheduled';
      case 'in-progress':
        return 'In Progress';
      case 'arriving':
        return 'Arriving';
      default:
        return status;
    }
  }

  startRide(rideId: number): void {
    console.log('Starting ride:', rideId);
    // Logic to start the ride
  }

  cancelRide(rideId: number): void {
    if (confirm('Are you sure you want to cancel this ride?')) {
      console.log('Cancelling ride:', rideId);
      // Logic to cancel the ride
    }
  }
}
