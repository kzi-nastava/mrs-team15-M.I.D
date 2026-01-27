import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserHistoryTable } from './user-history-table';

describe('UserHistoryTable', () => {
  let component: UserHistoryTable;
  let fixture: ComponentFixture<UserHistoryTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserHistoryTable]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserHistoryTable);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
