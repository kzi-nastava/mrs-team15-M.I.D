import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideEstimation } from './ride-estimation';

describe('RideEstimation', () => {
  let component: RideEstimation;
  let fixture: ComponentFixture<RideEstimation>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideEstimation]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideEstimation);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
