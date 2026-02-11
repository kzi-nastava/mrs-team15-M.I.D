import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminHistoryOverview } from './admin-history-overview';

describe('AdminHistoryOverview', () => {
  let component: AdminHistoryOverview;
  let fixture: ComponentFixture<AdminHistoryOverview>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminHistoryOverview]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminHistoryOverview);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
