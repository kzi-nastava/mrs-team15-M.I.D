import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChangeRequest } from './change-request';

describe('ChangeRequest', () => {
  let component: ChangeRequest;
  let fixture: ComponentFixture<ChangeRequest>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChangeRequest]
    }).compileComponents();

    fixture = TestBed.createComponent(ChangeRequest);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
