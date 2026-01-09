import { Component, ViewChild, ElementRef } from '@angular/core';
import { InputComponent } from '../../../shared/components/input-component/input-component';
import { Button } from '../../../shared/components/button/button';
import { RouterLink } from '@angular/router';
import { FromValidator } from '../../../shared/components/form-validator';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-registration-form',
  imports: [Button, RouterLink, InputComponent, CommonModule],
  templateUrl: './registration-form.html',
  styleUrl: './registration-form.css',
})

export class RegistrationForm {
  passwordVisible = false;
  confirmPasswordVisible = false;
  togglePassword(type: string) {
    const inputs= document.querySelectorAll<HTMLInputElement>('.password-input-wrapper input');
    if (type === 'new'){
      this.passwordVisible = !this.passwordVisible;
      if(inputs[0]) {
        inputs[0].type = this.passwordVisible ? 'text' : 'password';
      }
    } else if(type === 'confirm'){
      this.confirmPasswordVisible = !this.confirmPasswordVisible;
      if(inputs[1]) {
        inputs[1].type = this.confirmPasswordVisible ? 'text' : 'password';
      }
    }
  }

  firstName : string = '';
  lastName : string = '';
  phoneNumber : string = '';
  address : string = '';
  password : string = '';
  confirmedPassword : string = '';
  email : string = '';

  validator : FromValidator = new FromValidator();

  hasErrors(): boolean {
  return !!(
    this.validator.firstNameError(this.firstName) || this.validator.lastNameError(this.lastName) ||
    this.validator.emailError(this.email) || this.validator.phoneError(this.phoneNumber) ||
    this.validator.addressError(this.address) ||
    this.validator.passwordError(this.password) ||
    this.validator.confirmPasswordError(this.password, this.confirmedPassword));
  }

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  selectedFile: File | null = null;

  openFilePicker(): void {
    this.fileInput.nativeElement.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      return;
    }
    this.selectedFile = input.files[0];
    console.log(this.selectedFile);
  }
}

