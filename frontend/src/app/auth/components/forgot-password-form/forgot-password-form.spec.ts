import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ForgotPasswordForm } from './forgot-password-form';

describe('ForgotPasswordForm', () => {
  let component: ForgotPasswordForm;
  let fixture: ComponentFixture<ForgotPasswordForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ForgotPasswordForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ForgotPasswordForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
