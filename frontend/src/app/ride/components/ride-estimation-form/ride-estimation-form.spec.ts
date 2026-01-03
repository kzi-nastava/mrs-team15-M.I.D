import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideEstimationForm } from './ride-estimation-form';

describe('RideEstimationForm', () => {
  let component: RideEstimationForm;
  let fixture: ComponentFixture<RideEstimationForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideEstimationForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideEstimationForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
