import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpcomingRides } from './upcoming-rides';

describe('UpcomingRides', () => {
  let component: UpcomingRides;
  let fixture: ComponentFixture<UpcomingRides>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpcomingRides]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpcomingRides);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
