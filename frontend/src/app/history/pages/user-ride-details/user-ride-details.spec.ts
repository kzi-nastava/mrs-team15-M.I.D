import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserRideDetails } from './user-ride-details';

describe('UserRideDetails', () => {
  let component: UserRideDetails;
  let fixture: ComponentFixture<UserRideDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserRideDetails]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserRideDetails);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
