import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsersOverviewTable } from './users-overview-table';

describe('UsersOverviewTable', () => {
  let component: UsersOverviewTable;
  let fixture: ComponentFixture<UsersOverviewTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsersOverviewTable]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsersOverviewTable);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
