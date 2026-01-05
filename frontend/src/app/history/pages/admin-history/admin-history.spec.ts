import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminHistory } from './admin-history';

describe('AdminHistory', () => {
  let component: AdminHistory;
  let fixture: ComponentFixture<AdminHistory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminHistory]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminHistory);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
