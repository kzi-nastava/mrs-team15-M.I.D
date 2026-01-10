import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveRideWarningModal } from './active-ride-warning-modal';

describe('ActiveRideWarningModal', () => {
  let component: ActiveRideWarningModal;
  let fixture: ComponentFixture<ActiveRideWarningModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiveRideWarningModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActiveRideWarningModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
