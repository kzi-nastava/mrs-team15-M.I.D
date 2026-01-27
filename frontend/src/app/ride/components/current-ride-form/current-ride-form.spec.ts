import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CurrentRideForm } from './current-ride-form';

describe('CurrentRideForm', () => {
  let component: CurrentRideForm;
  let fixture: ComponentFixture<CurrentRideForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CurrentRideForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CurrentRideForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
