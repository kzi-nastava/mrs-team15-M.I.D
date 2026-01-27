import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PanicModal } from './panic-modal';

describe('PanicModal', () => {
  let component: PanicModal;
  let fixture: ComponentFixture<PanicModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PanicModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PanicModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
