import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MissingPassengersModal } from './missing-passengers-modal';

describe('MissingPassengersModal', () => {
  let component: MissingPassengersModal;
  let fixture: ComponentFixture<MissingPassengersModal>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MissingPassengersModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MissingPassengersModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
