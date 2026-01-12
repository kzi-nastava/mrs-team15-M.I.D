import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverRequestsPage } from './driver-requests';

describe('DriverRequests', () => {
  let component: DriverRequestsPage;
  let fixture: ComponentFixture<DriverRequestsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverRequestsPage]
    }).compileComponents();

    fixture = TestBed.createComponent(DriverRequestsPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
