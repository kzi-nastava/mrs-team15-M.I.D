import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverAppointedRides } from './driver-appointed-rides';

describe('DriverAppointedRides', () => {
  let component: DriverAppointedRides;
  let fixture: ComponentFixture<DriverAppointedRides>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverAppointedRides]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverAppointedRides);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
