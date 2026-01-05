import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReorderRideModal } from './reorder-ride-modal';

describe('ReorderRideModal', () => {
  let component: ReorderRideModal;
  let fixture: ComponentFixture<ReorderRideModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReorderRideModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReorderRideModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
