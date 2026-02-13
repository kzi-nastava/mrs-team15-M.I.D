import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RegistrationForm } from './registration-form';
import { of, throwError } from 'rxjs';

// run this test with this command :  ng test --include src/app/auth/components/registration-form/registration-form.spec.ts
describe('RegistrationForm', () => {
  let component: RegistrationForm;
  let fixture: ComponentFixture<RegistrationForm>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistrationForm],
      providers:[provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
    fixture = TestBed.createComponent(RegistrationForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty form fields', () => {
    expect(component.firstName).toBe('');
    expect(component.lastName).toBe('');
    expect(component.phoneNumber).toBe('');
    expect(component.address).toBe('');
    expect(component.password).toBe('');
    expect(component.confirmedPassword).toBe('');
    expect(component.email).toBe('');
  });

  it('should initialize with password hidden', () => {
    expect(component.passwordVisible).toBe(false);
    expect(component.confirmPasswordVisible).toBe(false);
  });

  it('should return false when all fields are valid', ()=> {
    component.firstName = 'Petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+381640000000';
    component.address = 'Bulevar Oslobođenja 123, Novi Sad';
    component.password = '123123';
    component.confirmedPassword = '123123';
    component.email = 'petarpetrovic@gmail.com';

    let result : boolean = component.hasErrors();

    expect(result).toBe(false);
  });

  it('should return true when firstName is invalid', ()=> {
    component.firstName = 'petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+381640000000';
    component.address = 'Bulevar Oslobođenja 123, Novi Sad';
    component.password = '123123';
    component.confirmedPassword = '123123';
    component.email = 'petarpetrovic@gmail.com';

    let result : boolean = component.hasErrors();

    expect(result).toBe(true);
  });

  it('should return true when email is invalid', ()=> {
    component.firstName = 'Petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+381640000000';
    component.address = 'Bulevar Oslobođenja 123, Novi Sad';
    component.password = '123123';
    component.confirmedPassword = '123123';
    component.email = 'petarpetrovicgmail.com';

    let result : boolean = component.hasErrors();

    expect(result).toBe(true);
  });

  it('should return true when passwords do not match', ()=> {
    component.firstName = 'Petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+381640000000';
    component.address = 'Bulevar Oslobođenja 123, Novi Sad';
    component.password = '123123123';
    component.confirmedPassword = '123123';
    component.email = 'petarpetrovic@gmail.com';

    let result : boolean = component.hasErrors();

    expect(result).toBe(true);
  });

  it('should return true when password is too short', ()=> {
    component.firstName = 'Petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+381640000000';
    component.address = 'Bulevar Oslobođenja 123, Novi Sad';
    component.password = '12312';
    component.confirmedPassword = '12312';
    component.email = 'petarpetrovic@gmail.com';

    let result : boolean = component.hasErrors();

    expect(result).toBe(true);
  });

  it('should return true when phone number is invalid', ()=> {
    component.firstName = 'Petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+382640000000';
    component.address = 'Bulevar Oslobođenja 123, Novi Sad';
    component.password = '123123';
    component.confirmedPassword = '123123';
    component.email = 'petarpetrovic@gmail.com';

    let result : boolean = component.hasErrors();

    expect(result).toBe(true);
  });

  it('should return true when address is too short', ()=> {
    component.firstName = 'Petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+381640000000';
    component.address = 'Adr';
    component.password = '123123';
    component.confirmedPassword = '123123';
    component.email = 'petarpetrovic@gmail.com';

    let result : boolean = component.hasErrors();

    expect(result).toBe(true);
  });

  it('should toggle new password visibility', () =>{
    expect(component.passwordVisible).toBe(false);

    component.togglePassword('new');

    expect(component.passwordVisible).toBe(true);
  });

  it('should toggle new password visibility back to hidden', () =>{
    component.passwordVisible = true;

    component.togglePassword('new');

    expect(component.passwordVisible).toBe(false);
    });

    it('should toggle confirm password visibility', () =>{
    expect(component.confirmPasswordVisible).toBe(false);

    component.togglePassword('confirm');

    expect(component.confirmPasswordVisible).toBe(true);
  });

  it('should toggle confirm password visibility back to hidden', () =>{
    component.confirmPasswordVisible = true;

    component.togglePassword('confirm');

    expect(component.confirmPasswordVisible).toBe(false);
  });

  it('should store selected file', () => {
    const mockFile = new File(['image content'], 'profile.jpg', { type: 'image/jpeg' });
    const mockEvent = {
    target: { files: [mockFile] }
    } as any;

    component.onFileSelected(mockEvent);

    expect(component.selectedFile).toBe(mockFile);
    expect(component.selectedFile?.name).toBe('profile.jpg');
  });

  it('should not store file when no file is selected', () => {
    const mockEvent = {
    target: { files: [] }
    } as any;

    component.onFileSelected(mockEvent);

    expect(component.selectedFile).toBe(null);
    });

    it('should not store file when files is null', () => {
    const mockEvent = {
    target: { files: null }
    } as any;

    component.onFileSelected(mockEvent);

    expect(component.selectedFile).toBe(null);
    });

    it('should set message and show toast', ()=> {
    const testMessage = "Test message";

    component.showMessageToast(testMessage);

    expect(component.showMessage).toBe(true);
    expect(component.message).toBe(testMessage)
    });

    it('should hide toast after 3 seconds', fakeAsync(() => {
    const testMessage = "Test message";

    component.showMessageToast(testMessage);

    expect(component.showMessage).toBe(true);
    tick(3000);
    expect(component.showMessage).toBe(false);
    }));

    it('should call authService.register with correct data when form is valid', () => {
    component.firstName = 'Petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+381640000000';
    component.address = 'Bulevar Oslobođenja 123, Novi Sad';
    component.password = '123123';
    component.confirmedPassword = '123123';
    component.email = 'petarpetrovic@gmail.com';

    const registerSpy = spyOn(component['authService'], 'register')
    .and.returnValue(of({ success: true }));

    component.signUp();

    expect(registerSpy).toHaveBeenCalled();
    const formData = registerSpy.calls.mostRecent().args[0] as FormData;
    expect(formData.get('firstName')).toBe('Petar');
    expect(formData.get('lastName')).toBe('Petrović');
    expect(formData.get('phoneNumber')).toBe('+381640000000');
    expect(formData.get('address')).toBe('Bulevar Oslobođenja 123, Novi Sad');
    expect(formData.get('password')).toBe('123123');
    expect(formData.get('confirmPassword')).toBe('123123');
    expect(formData.get('email')).toBe('petarpetrovic@gmail.com');
  });

  it('should include profile image in FormData when file is selected', () => {
    component.firstName = 'Petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+381640000000';
    component.address = 'Bulevar Oslobođenja 123, Novi Sad';
    component.password = '123123';
    component.confirmedPassword = '123123';
    component.email = 'petarpetrovic@gmail.com';

    const mockFile = new File(['image'], 'profile.jpg', { type: 'image/jpeg' });
    component.selectedFile = mockFile;

    const registerSpy = spyOn(component['authService'], 'register')
    .and.returnValue(of({ success: true }));

    component.signUp();

    expect(registerSpy).toHaveBeenCalled();
    const formData = registerSpy.calls.mostRecent().args[0] as FormData;
    expect(formData.get('firstName')).toBe('Petar');
    expect(formData.get('lastName')).toBe('Petrović');
    expect(formData.get('phoneNumber')).toBe('+381640000000');
    expect(formData.get('address')).toBe('Bulevar Oslobođenja 123, Novi Sad');
    expect(formData.get('password')).toBe('123123');
    expect(formData.get('confirmPassword')).toBe('123123');
    expect(formData.get('email')).toBe('petarpetrovic@gmail.com');
    expect(formData.get('profileImage')).toBe(mockFile);
  });

  it('should not call authService when form has errors', ()=>{
    component.firstName = 'Petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+389640000000';
    component.address = 'Bulevar Oslobođenja 123, Novi Sad';
    component.password = '12313';
    component.confirmedPassword = '123123';
    component.email = 'petarpetrovicgmail.com';

    const registerSpy = spyOn(component['authService'], 'register');

    component.signUp();

    expect(registerSpy).not.toHaveBeenCalled();
  });

  it('should show success message and navigate to login after successful registration', fakeAsync(() =>{
    component.firstName = 'Petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+381640000000';
    component.address = 'Bulevar Oslobođenja 123, Novi Sad';
    component.password = '123123';
    component.confirmedPassword = '123123';
    component.email = 'petarpetrovic@gmail.com';

    spyOn(component['authService'], 'register').and.returnValue(of({ success: true }));
    spyOn(component, 'showMessageToast');
    const navigateSpy = spyOn(component['router'], 'navigate');

    component.signUp();
    tick(4000);

    expect(component.showMessageToast).toHaveBeenCalledWith('Registration successful! Please check your email and activate your account using the link sent to you.');
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  }));

  it('should show error message when registration fails with string error', () => {
    component.firstName = 'Petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+381640000000';
    component.address = 'Bulevar Oslobođenja 123, Novi Sad';
    component.password = '123123';
    component.confirmedPassword = '123123';
    component.email = 'petarpetrovic@gmail.com';

    const errorResponse = { error: 'Email already exists' };
    spyOn(component['authService'], 'register').and.returnValue(throwError(()=> errorResponse));
    spyOn(component, 'showMessageToast');

    component.signUp();
    expect(component.showMessageToast).toHaveBeenCalledWith('Email already exists');
  });

  it('should show generic error when registration fails with non-string error', () => {
    component.firstName = 'Petar';
    component.lastName = 'Petrović';
    component.phoneNumber = '+381640000000';
    component.address = 'Bulevar Oslobođenja 123, Novi Sad';
    component.password = '123123';
    component.confirmedPassword = '123123';
    component.email = 'petarpetrovic@gmail.com';

    const errorResponse = { error: { message: 'Something went wrong' } };
    spyOn(component['authService'], 'register').and.returnValue(throwError(()=> errorResponse));
    spyOn(component, 'showMessageToast');

    component.signUp();
    expect(component.showMessageToast).toHaveBeenCalledWith('Registration failed. Please try again.');
  });
});