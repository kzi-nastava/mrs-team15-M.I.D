import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideOrdering } from './ride-ordering';

describe('RideOrdering', () => {
  let component: RideOrdering;
  let fixture: ComponentFixture<RideOrdering>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideOrdering]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideOrdering);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
