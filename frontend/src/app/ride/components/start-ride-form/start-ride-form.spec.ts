import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StartRideForm } from './start-ride-form';

describe('StartRideForm', () => {
  let component: StartRideForm;
  let fixture: ComponentFixture<StartRideForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StartRideForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StartRideForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
