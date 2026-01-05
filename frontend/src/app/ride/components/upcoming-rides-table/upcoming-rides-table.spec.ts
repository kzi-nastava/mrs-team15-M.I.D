import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpcomingRidesTable } from './upcoming-rides-table';

describe('UpcomingRidesTable', () => {
  let component: UpcomingRidesTable;
  let fixture: ComponentFixture<UpcomingRidesTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpcomingRidesTable]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpcomingRidesTable);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
