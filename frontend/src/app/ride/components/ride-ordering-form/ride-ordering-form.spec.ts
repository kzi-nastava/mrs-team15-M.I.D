import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideOrderingForm } from './ride-ordering-form';

describe('RideOrderingForm', () => {
  let component: RideOrderingForm;
  let fixture: ComponentFixture<RideOrderingForm>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideOrderingForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideOrderingForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
