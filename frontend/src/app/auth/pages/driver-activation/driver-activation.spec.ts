import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverActivation } from './driver-activation';

describe('DriverActivation', () => {
  let component: DriverActivation;
  let fixture: ComponentFixture<DriverActivation>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverActivation]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverActivation);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
