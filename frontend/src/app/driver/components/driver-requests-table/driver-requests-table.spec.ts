import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverRequestsTable } from './driver-requests-table';

describe('DriverRequestsTable', () => {
  let component: DriverRequestsTable;
  let fixture: ComponentFixture<DriverRequestsTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverRequestsTable]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverRequestsTable);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
