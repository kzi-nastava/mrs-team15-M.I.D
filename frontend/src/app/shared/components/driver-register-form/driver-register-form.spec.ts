import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { DriverRegisterForm } from './driver-register-form';
import { of, throwError } from 'rxjs';

// command: ng test --include src/app/shared/components/driver-register-form/driver-register-form.spec.ts
describe('DriverRegisterForm', () => {
  let component: DriverRegisterForm;
  let fixture: ComponentFixture<DriverRegisterForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverRegisterForm],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(DriverRegisterForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty form fields', () => {
    expect(component.user.firstName).toBe('');
    expect(component.user.lastName).toBe('');
    expect(component.user.phone).toBe('');
    expect(component.user.address).toBe('');
    expect(component.user.email).toBe('');
    expect(component.user.role).toBe('driver');
    expect(component.user.activeHours).toBe(0);
  });

  it('should initialize with empty vehicle fields', () => {
    expect(component.vehicle.licensePlate).toBe('');
    expect(component.vehicle.model).toBe('');
    expect(component.vehicle.seats).toBeUndefined();
    expect(component.vehicle.type).toBe('Standard');
    expect(component.vehicle.petFriendly).toBe(false);
    expect(component.vehicle.babyFriendly).toBe(false);
  });

  it('should initialize with empty user avatar', () => {
    expect(component.userAvatar).toBe('');
  });

  it('should initialize with toast hidden', () => {
    expect(component.showToast).toBe(false);
    expect(component.toastMessage).toBe('');
    expect(component.toastType).toBe('success');
  });

  // ngOnInit tests
  describe('ngOnInit', () => {
    it('should initialize component', () => {
      spyOn(console, 'log');
      component.ngOnInit();
      // ngOnInit currently just checks for ADMIN role
      expect(component).toBeTruthy();
    });
  });

  // File input tests
  describe('File Input', () => {
    it('should trigger file input click when onSelectPhoto is called', () => {
      const mockClick = jasmine.createSpy('click');
      component.fileInput = {
        nativeElement: {
          click: mockClick
        }
      } as any;

      component.onSelectPhoto();
      expect(mockClick).toHaveBeenCalled();
    });
  });

  // Email validation tests
  describe('Email Validation', () => {
    it('should return true for valid email', () => {
      expect(component.isEmailValid('test@example.com')).toBe(true);
      expect(component.isEmailValid('user.name@domain.co.rs')).toBe(true);
      expect(component.isEmailValid('test123@test.org')).toBe(true);
    });

    it('should return false for invalid email', () => {
      expect(component.isEmailValid('invalid')).toBe(false);
      expect(component.isEmailValid('test@')).toBe(false);
      expect(component.isEmailValid('@example.com')).toBe(false);
      expect(component.isEmailValid('test@.com')).toBe(false);
      expect(component.isEmailValid('')).toBe(false);
    });

    it('should return correct email error message when email is empty', () => {
      component.user.email = '';
      expect(component.getEmailErrorMessage()).toBe('Email is required');
    });

    it('should return correct email error message when email is invalid', () => {
      component.user.email = 'invalidemail';
      expect(component.getEmailErrorMessage()).toBe('Email is invalid');
    });
  });

  // Phone validation tests
  describe('Phone Validation', () => {
    it('should return true for valid phone numbers', () => {
      expect(component.isPhoneValid('0601234567')).toBe(true);
      expect(component.isPhoneValid('+381601234567')).toBe(true);
      expect(component.isPhoneValid('0641234567')).toBe(true);
      expect(component.isPhoneValid('+381641234567')).toBe(true);
    });

    it('should return false for invalid phone numbers', () => {
      expect(component.isPhoneValid('123')).toBe(false);
      expect(component.isPhoneValid('+382601234567')).toBe(false);
      expect(component.isPhoneValid('1234567890')).toBe(false);
      expect(component.isPhoneValid('')).toBe(false);
    });

    it('should return correct phone error message when phone is empty', () => {
      component.user.phone = '';
      expect(component.getPhoneErrorMessage()).toBe('Phone number is required');
    });

    it('should return correct phone error message when phone is invalid', () => {
      component.user.phone = '123456';
      expect(component.getPhoneErrorMessage()).toBe('Phone number is invalid (e.g: 0601234567 or +381601234567)');
    });
  });

  // License plate validation tests
  describe('License Plate Validation', () => {
    it('should return true for valid license plates', () => {
      expect(component.isLicensePlateValid('NS123AB')).toBe(true);
      expect(component.isLicensePlateValid('BG456CD')).toBe(true);
      expect(component.isLicensePlateValid('ns123ab')).toBe(true); // should handle lowercase
    });

    it('should return false for invalid license plates', () => {
      expect(component.isLicensePlateValid('NS1234AB')).toBe(false);
      expect(component.isLicensePlateValid('NS12AB')).toBe(false);
      expect(component.isLicensePlateValid('N123AB')).toBe(false);
      expect(component.isLicensePlateValid('NS123A')).toBe(false);
      expect(component.isLicensePlateValid('')).toBe(false);
    });

    it('should return correct license plate error message when empty', () => {
      component.vehicle.licensePlate = '';
      expect(component.getLicensePlateErrorMessage()).toBe('License plate is required');
    });

    it('should return correct license plate error message when invalid format', () => {
      component.vehicle.licensePlate = 'INVALID';
      expect(component.getLicensePlateErrorMessage()).toBe('License plate format is invalid (e.g: NS123AB)');
    });
  });

  // Vehicle fields validation tests
  describe('Vehicle Fields Validation', () => {
    it('should return correct model error message when model is empty', () => {
      component.vehicle.model = '';
      expect(component.getModelErrorMessage()).toBe('Car model is required');
    });

    it('should return correct seats error message when seats is empty', () => {
      component.vehicle.seats = undefined;
      expect(component.getSeatsErrorMessage()).toBe('Seats is required');
    });

    it('should return correct seats error message when seats is less than 1', () => {
      component.vehicle.seats = 0;
      expect(component.getSeatsErrorMessage()).toBe('Seats must be at least 1');
    });

    it('should return correct type error message when type is empty', () => {
      component.vehicle.type = '';
      expect(component.getTypeErrorMessage()).toBe('Type is required');
    });

    it('should detect empty field correctly', () => {
      expect(component.isFieldEmpty('')).toBe(true);
      expect(component.isFieldEmpty('  ')).toBe(true);
      expect(component.isFieldEmpty('test')).toBe(false);
    });

    it('should detect empty vehicle field correctly', () => {
      expect(component.isVehicleFieldEmpty('')).toBe(true);
      expect(component.isVehicleFieldEmpty('  ')).toBe(true);
      expect(component.isVehicleFieldEmpty('test')).toBe(false);
    });
  });

  // File selection tests
  describe('File Selection', () => {
    it('should update userAvatar and selectedFile when file is selected', () => {
      const mockFile = new File(['image content'], 'avatar.jpg', { type: 'image/jpeg' });
      const mockEvent = {
        target: { files: [mockFile] }
      } as any;

      spyOn(URL, 'createObjectURL').and.returnValue('blob:mock-url');
      component.onFileSelected(mockEvent);

      expect(component.userAvatar).toBe('blob:mock-url');
      expect(component.selectedFile).toBe(mockFile);
    });

    it('should not update userAvatar when no file is selected', () => {
      const mockEvent = {
        target: { files: [] }
      } as any;

      component.onFileSelected(mockEvent);
      expect(component.userAvatar).toBe('');
    });
  });

  // Vehicle type mapping tests
  describe('Vehicle Type Mapping', () => {
    it('should map Standard type correctly', () => {
      const result = component['mapVehicleType']('Standard');
      expect(result).toBe('STANDARD');
    });

    it('should map Luksuz type to LUXURY', () => {
      expect(component['mapVehicleType']('Luksuz')).toBe('LUXURY');
      expect(component['mapVehicleType']('luxury')).toBe('LUXURY');
      expect(component['mapVehicleType']('lux')).toBe('LUXURY');
    });

    it('should map Kombi type to VAN', () => {
      expect(component['mapVehicleType']('Kombi')).toBe('VAN');
      expect(component['mapVehicleType']('van')).toBe('VAN');
    });

    it('should default to STANDARD for unknown types', () => {
      expect(component['mapVehicleType']('unknown')).toBe('STANDARD');
      expect(component['mapVehicleType']('')).toBe('STANDARD');
    });
  });

  // Form submission validation tests
  describe('Form Submission Validation', () => {
    it('should not submit when personal fields are invalid', () => {
      component.user.firstName = '';
      component.user.lastName = 'Petrovic';
      component.user.email = 'test@example.com';
      component.user.phone = '0601234567';
      component.vehicle.licensePlate = 'NS123AB';
      component.vehicle.model = 'Toyota';
      component.vehicle.seats = 4;

      const registerSpy = spyOn(component['adminService'], 'registerDriver');
      component.onSubmit();

      expect(registerSpy).not.toHaveBeenCalled();
      expect(component.toastType).toBe('error');
    });

    it('should not submit when email is invalid', () => {
      component.user.firstName = 'Petar';
      component.user.lastName = 'Petrovic';
      component.user.email = 'invalidemail';
      component.user.phone = '0601234567';
      component.vehicle.licensePlate = 'NS123AB';
      component.vehicle.model = 'Toyota';
      component.vehicle.seats = 4;

      const registerSpy = spyOn(component['adminService'], 'registerDriver');
      component.onSubmit();

      expect(registerSpy).not.toHaveBeenCalled();
      expect(component.toastType).toBe('error');
    });

    it('should not submit when phone is invalid', () => {
      component.user.firstName = 'Petar';
      component.user.lastName = 'Petrovic';
      component.user.email = 'test@example.com';
      component.user.phone = '123';
      component.vehicle.licensePlate = 'NS123AB';
      component.vehicle.model = 'Toyota';
      component.vehicle.seats = 4;

      const registerSpy = spyOn(component['adminService'], 'registerDriver');
      component.onSubmit();

      expect(registerSpy).not.toHaveBeenCalled();
      expect(component.toastType).toBe('error');
    });

    it('should not submit when vehicle fields are invalid', () => {
      component.user.firstName = 'Petar';
      component.user.lastName = 'Petrovic';
      component.user.email = 'test@example.com';
      component.user.phone = '0601234567';
      component.vehicle.licensePlate = ''; // empty
      component.vehicle.model = 'Toyota';
      component.vehicle.seats = 4;

      const registerSpy = spyOn(component['adminService'], 'registerDriver');
      component.onSubmit();

      expect(registerSpy).not.toHaveBeenCalled();
      expect(component.toastType).toBe('error');
    });

    it('should not submit when license plate is invalid format', () => {
      component.user.firstName = 'Petar';
      component.user.lastName = 'Petrovic';
      component.user.email = 'test@example.com';
      component.user.phone = '0601234567';
      component.vehicle.licensePlate = 'INVALID';
      component.vehicle.model = 'Toyota';
      component.vehicle.seats = 4;

      const registerSpy = spyOn(component['adminService'], 'registerDriver');
      component.onSubmit();

      expect(registerSpy).not.toHaveBeenCalled();
      expect(component.toastType).toBe('error');
    });
  });

  // Form submission with valid data tests
  describe('Form Submission with Valid Data', () => {
    beforeEach(() => {
      component.user.firstName = 'Petar';
      component.user.lastName = 'Petrovic';
      component.user.email = 'test@example.com';
      component.user.phone = '0601234567';
      component.user.address = 'Bulevar Oslobodjenja 123, Novi Sad';
      component.vehicle.licensePlate = 'NS123AB';
      component.vehicle.model = 'Toyota Corolla';
      component.vehicle.seats = 4;
      component.vehicle.type = 'Standard';
      component.vehicle.petFriendly = true;
      component.vehicle.babyFriendly = false;
    });

    it('should call adminService.registerDriver with FormData when form is valid', () => {
      const registerSpy = spyOn(component['adminService'], 'registerDriver')
        .and.returnValue(of({ success: true }));

      component.onSubmit();

      expect(registerSpy).toHaveBeenCalled();
      const formData = registerSpy.calls.mostRecent().args[0];
      expect(formData instanceof FormData).toBe(true);
      expect(formData.get('email')).toBe('test@example.com');
      expect(formData.get('firstName')).toBe('Petar');
      expect(formData.get('lastName')).toBe('Petrovic');
      expect(formData.get('phoneNumber')).toBe('0601234567');
      expect(formData.get('address')).toBe('Bulevar Oslobodjenja 123, Novi Sad');
      expect(formData.get('licensePlate')).toBe('NS123AB');
      expect(formData.get('vehicleModel')).toBe('Toyota Corolla');
      expect(formData.get('vehicleType')).toBe('STANDARD');
      expect(formData.get('numberOfSeats')).toBe('4');
      expect(formData.get('petFriendly')).toBe('true');
      expect(formData.get('babyFriendly')).toBe('false');
    });

    it('should send profileImage in FormData when file is selected', () => {
      const mockFile = new File(['image content'], 'avatar.jpg', { type: 'image/jpeg' });
      component.selectedFile = mockFile;
      
      const registerSpy = spyOn(component['adminService'], 'registerDriver')
        .and.returnValue(of({ success: true }));

      component.onSubmit();

      expect(registerSpy).toHaveBeenCalled();
      const formData = registerSpy.calls.mostRecent().args[0];
      expect(formData instanceof FormData).toBe(true);
      expect(formData.get('profileImage')).toEqual(mockFile);
    });

    it('should not send profileImage when no file is selected', () => {
      component.selectedFile = null;
      const registerSpy = spyOn(component['adminService'], 'registerDriver')
        .and.returnValue(of({ success: true }));

      component.onSubmit();

      expect(registerSpy).toHaveBeenCalled();
      const formData = registerSpy.calls.mostRecent().args[0];
      expect(formData instanceof FormData).toBe(true);
      expect(formData.get('profileImage')).toBeNull();
    });

    it('should map vehicle type to LUXURY correctly', () => {
      component.vehicle.type = 'Luksuz';
      const registerSpy = spyOn(component['adminService'], 'registerDriver')
        .and.returnValue(of({ success: true }));

      component.onSubmit();

      expect(registerSpy).toHaveBeenCalled();
      const formData = registerSpy.calls.mostRecent().args[0];
      expect(formData instanceof FormData).toBe(true);
      expect(formData.get('vehicleType')).toBe('LUXURY');
    });

    it('should map vehicle type to VAN correctly', () => {
      component.vehicle.type = 'Kombi';
      const registerSpy = spyOn(component['adminService'], 'registerDriver')
        .and.returnValue(of({ success: true }));

      component.onSubmit();

      expect(registerSpy).toHaveBeenCalled();
      const formData = registerSpy.calls.mostRecent().args[0];
      expect(formData instanceof FormData).toBe(true);
      expect(formData.get('vehicleType')).toBe('VAN');
    });

    it('should convert boolean vehicle features correctly', () => {
      component.vehicle.petFriendly = false;
      component.vehicle.babyFriendly = true;
      const registerSpy = spyOn(component['adminService'], 'registerDriver')
        .and.returnValue(of({ success: true }));

      component.onSubmit();

      expect(registerSpy).toHaveBeenCalled();
      const formData = registerSpy.calls.mostRecent().args[0];
      expect(formData instanceof FormData).toBe(true);
      expect(formData.get('petFriendly')).toBe('false');
      expect(formData.get('babyFriendly')).toBe('true');
    });
  });

  // Success/Error handling tests
  describe('Success and Error Handling', () => {
    beforeEach(() => {
      component.user.firstName = 'Petar';
      component.user.lastName = 'Petrovic';
      component.user.email = 'test@example.com';
      component.user.phone = '0601234567';
      component.user.address = 'Bulevar Oslobodjenja 123, Novi Sad';
      component.vehicle.licensePlate = 'NS123AB';
      component.vehicle.model = 'Toyota Corolla';
      component.vehicle.seats = 4;
    });

    it('should show success toast when registration succeeds', fakeAsync(() => {
      spyOn(component['adminService'], 'registerDriver')
        .and.returnValue(of({ success: true }));

      component.onSubmit();
      tick();

      expect(component.showToast).toBe(true);
      expect(component.toastType).toBe('success');
      expect(component.toastMessage).toBe('Driver registration submitted');
    }));

    it('should hide toast after 3 seconds on success', fakeAsync(() => {
      spyOn(component['adminService'], 'registerDriver')
        .and.returnValue(of({ success: true }));

      component.onSubmit();
      tick();
      expect(component.showToast).toBe(true);

      tick(3000);
      expect(component.showToast).toBe(false);
    }));

    it('should show error toast when registration fails', fakeAsync(() => {
      const errorResponse = { error: 'Registration failed' };
      spyOn(component['adminService'], 'registerDriver')
        .and.returnValue(throwError(() => errorResponse));

      component.onSubmit();
      tick();

      expect(component.showToast).toBe(true);
      expect(component.toastType).toBe('error');
      expect(component.toastMessage).toBe('Driver registration failed');
    }));

    it('should hide toast after 3 seconds on error', fakeAsync(() => {
      const errorResponse = { error: 'Registration failed' };
      spyOn(component['adminService'], 'registerDriver')
        .and.returnValue(throwError(() => errorResponse));

      component.onSubmit();
      tick();
      expect(component.showToast).toBe(true);

      tick(3000);
      expect(component.showToast).toBe(false);
    }));
  });

  // Data logging tests (verify console.log calls)
  describe('Data Logging', () => {
    it('should log form data when onSubmit is called', () => {
      component.user.firstName = 'Petar';
      component.user.lastName = 'Petrovic';
      component.user.email = 'test@example.com';
      component.user.phone = '0601234567';
      component.user.address = 'Bulevar Oslobodjenja 123, Novi Sad';
      component.vehicle.licensePlate = 'NS123AB';
      component.vehicle.model = 'Toyota Corolla';
      component.vehicle.seats = 4;

      spyOn(console, 'log');
      spyOn(component['adminService'], 'registerDriver')
        .and.returnValue(of({ success: true }));

      component.onSubmit();

      expect(console.log).toHaveBeenCalledWith(
        'DriverRegisterForm.onSubmit called',
        jasmine.objectContaining({
          user: jasmine.any(Object),
          vehicle: jasmine.any(Object)
        })
      );
    });

    it('should log payload before posting', () => {
      component.user.firstName = 'Petar';
      component.user.lastName = 'Petrovic';
      component.user.email = 'test@example.com';
      component.user.phone = '0601234567';
      component.user.address = 'Bulevar Oslobodjenja 123, Novi Sad';
      component.vehicle.licensePlate = 'NS123AB';
      component.vehicle.model = 'Toyota Corolla';
      component.vehicle.seats = 4;

      spyOn(console, 'log');
      spyOn(component['adminService'], 'registerDriver')
        .and.returnValue(of({ success: true }));

      component.onSubmit();

      expect(console.log).toHaveBeenCalledWith(
        'Posting driver registration with FormData',
        'adminId',
        jasmine.any(Number)
      );
    });
  });
});
