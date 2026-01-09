import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StopRideModal } from './stop-ride-modal';

describe('StopRideModal', () => {
  let component: StopRideModal;
  let fixture: ComponentFixture<StopRideModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StopRideModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StopRideModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
