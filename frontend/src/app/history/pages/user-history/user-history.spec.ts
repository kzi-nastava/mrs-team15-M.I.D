import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserHistory } from './user-history';

describe('UserHistory', () => {
  let component: UserHistory;
  let fixture: ComponentFixture<UserHistory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserHistory]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserHistory);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
