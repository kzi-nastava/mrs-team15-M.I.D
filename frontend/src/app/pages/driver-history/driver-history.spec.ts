import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverHistory } from './driver-history';

describe('DriverHistory', () => {
  let component: DriverHistory;
  let fixture: ComponentFixture<DriverHistory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverHistory]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverHistory);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
