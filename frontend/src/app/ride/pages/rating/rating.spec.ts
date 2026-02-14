import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Rating } from './rating';
import { of, throwError } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

// run this test with this command: ng test --include src/app/ride/pages/rating/rating.spec.ts
describe('Rating', () => {
  let component: Rating;
  let fixture: ComponentFixture<Rating>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Rating],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ id: '123' })
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Rating);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty rating fields', () => {
    expect(component.driverRating).toBe(0);
    expect(component.vehicleRating).toBe(0);
    expect(component.driverComment).toBe('');
    expect(component.vehicleComment).toBe('');
  });

  it('should extract rideId from route params on initialization', () => {
    component.ngOnInit();
    expect(component.rideId).toBe(123);
  });

  it('should update driver rating when onDriverRatingChange is called', () => {
    component.onDriverRatingChange(5);
    expect(component.driverRating).toBe(5);
  });

  it('should update vehicle rating when onVehicleRatingChange is called', () => {
    component.onVehicleRatingChange(4);
    expect(component.vehicleRating).toBe(4);
  });

  it('should return false when both ratings are zero', () => {
    component.driverRating = 0;
    component.vehicleRating = 0;

    const result = component.isRatingValid();

    expect(result).toBe(false);
  });

  it('should return false when driver rating is zero', () => {
    component.driverRating = 0;
    component.vehicleRating = 5;

    const result = component.isRatingValid();

    expect(result).toBe(false);
  });

  it('should return false when vehicle rating is zero', () => {
    component.driverRating = 5;
    component.vehicleRating = 0;

    const result = component.isRatingValid();

    expect(result).toBe(false);
  });

  it('should return true when both ratings are greater than zero', () => {
    component.driverRating = 5;
    component.vehicleRating = 4;

    const result = component.isRatingValid();

    expect(result).toBe(true);
  });

  it('should not submit rating when validation fails', () => {
    component.driverRating = 0;
    component.vehicleRating = 5;

    spyOn(window, 'alert');
    const rateRideSpy = spyOn(component['rideService'], 'rateRide');

    component.submitRating();

    expect(window.alert).toHaveBeenCalledWith('Please rate both the driver and the vehicle before submitting.');
    expect(rateRideSpy).not.toHaveBeenCalled();
  });

  it('should call rideService.rateRide with correct data when form is valid', () => {
    component.rideId = 123;
    component.driverRating = 5;
    component.vehicleRating = 4;
    component.driverComment = 'Excellent driver!';
    component.vehicleComment = 'Very clean vehicle.';

    const rateRideSpy = spyOn(component['rideService'], 'rateRide')
      .and.returnValue(of({ success: true }));

    component.submitRating();

    expect(rateRideSpy).toHaveBeenCalled();
    expect(rateRideSpy).toHaveBeenCalledWith(123, {
      driverRating: 5,
      vehicleRating: 4,
      driverComment: 'Excellent driver!',
      vehicleComment: 'Very clean vehicle.'
    });
  });

  it('should submit rating with empty comments when comments are not provided', () => {
    component.rideId = 456;
    component.driverRating = 3;
    component.vehicleRating = 4;
    component.driverComment = '';
    component.vehicleComment = '';

    const rateRideSpy = spyOn(component['rideService'], 'rateRide')
      .and.returnValue(of({ success: true }));

    component.submitRating();

    expect(rateRideSpy).toHaveBeenCalledWith(456, {
      driverRating: 3,
      vehicleRating: 4,
      driverComment: '',
      vehicleComment: ''
    });
  });

  it('should navigate to home after successful rating submission', () => {
    component.rideId = 123;
    component.driverRating = 5;
    component.vehicleRating = 4;

    spyOn(component['rideService'], 'rateRide').and.returnValue(of({ success: true }));
    const navigateSpy = spyOn(component['router'], 'navigate');

    component.submitRating();

    expect(navigateSpy).toHaveBeenCalledWith(['/home']);
  });

  it('should show error alert when rating submission fails', () => {
    component.rideId = 123;
    component.driverRating = 5;
    component.vehicleRating = 4;

    const errorResponse = { error: 'Failed to submit rating' };
    spyOn(component['rideService'], 'rateRide').and.returnValue(throwError(() => errorResponse));
    spyOn(window, 'alert');

    component.submitRating();

    expect(window.alert).toHaveBeenCalledWith('Failed to submit rating. Please try again.');
  });

  it('should navigate to home when skipRating is called', () => {
    const navigateSpy = spyOn(component['router'], 'navigate');

    component.skipRating();

    expect(navigateSpy).toHaveBeenCalledWith(['/home']);
  });

  it('should handle different rating values correctly', () => {
    component.onDriverRatingChange(1);
    component.onVehicleRatingChange(3);

    expect(component.driverRating).toBe(1);
    expect(component.vehicleRating).toBe(3);
    expect(component.isRatingValid()).toBe(true);
  });

  it('should update driver comment when changed', () => {
    component.driverComment = 'Great service!';
    expect(component.driverComment).toBe('Great service!');
  });

  it('should update vehicle comment when changed', () => {
    component.vehicleComment = 'Clean and comfortable!';
    expect(component.vehicleComment).toBe('Clean and comfortable!');
  });
});
