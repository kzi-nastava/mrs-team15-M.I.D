import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HistoryRideDetails } from './history-ride-details';

describe('HistoryRideDetails', () => {
  let component: HistoryRideDetails;
  let fixture: ComponentFixture<HistoryRideDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistoryRideDetails]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HistoryRideDetails);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
