import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChangeRequestForm } from './change-request-form';

describe('ChangeRequestForm', () => {
  let component: ChangeRequestForm;
  let fixture: ComponentFixture<ChangeRequestForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChangeRequestForm]
    }).compileComponents();

    fixture = TestBed.createComponent(ChangeRequestForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
