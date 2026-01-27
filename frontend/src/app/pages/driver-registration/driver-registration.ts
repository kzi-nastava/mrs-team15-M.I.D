import { Component } from '@angular/core';
import { DriverRegisterForm } from '../../shared/components/driver-register-form/driver-register-form';

@Component({
  selector: 'app-driver-registration',
  standalone: true,
  imports: [DriverRegisterForm],
  templateUrl: './driver-registration.html',
  styleUrl: './driver-registration.css',
})
export class DriverRegistration {}
