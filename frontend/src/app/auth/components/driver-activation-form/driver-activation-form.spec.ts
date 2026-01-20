import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverActivationForm } from './driver-activation-form';

describe('DriverActivationForm', () => {
  let component: DriverActivationForm;
  let fixture: ComponentFixture<DriverActivationForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverActivationForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverActivationForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
