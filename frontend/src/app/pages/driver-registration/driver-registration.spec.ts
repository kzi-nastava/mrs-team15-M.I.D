import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverRegistration } from './driver-registration';

describe('DriverRegistration', () => {
  let component: DriverRegistration;
  let fixture: ComponentFixture<DriverRegistration>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverRegistration]
    }).compileComponents();

    fixture = TestBed.createComponent(DriverRegistration);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
