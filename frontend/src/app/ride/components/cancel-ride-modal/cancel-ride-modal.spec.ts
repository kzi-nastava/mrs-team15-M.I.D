import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CancelRideModal } from './cancel-ride-modal';

describe('CancelRideModal', () => {
  let component: CancelRideModal;
  let fixture: ComponentFixture<CancelRideModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CancelRideModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CancelRideModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
