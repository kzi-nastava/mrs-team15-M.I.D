import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminHistoryTable } from './admin-history-table';

describe('AdminHistoryTable', () => {
  let component: AdminHistoryTable;
  let fixture: ComponentFixture<AdminHistoryTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminHistoryTable]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminHistoryTable);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
