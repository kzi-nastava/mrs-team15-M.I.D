import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResetPasswordForm } from './reset-password-form';

describe('ResetPasswordForm', () => {
  let component: ResetPasswordForm;
  let fixture: ComponentFixture<ResetPasswordForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResetPasswordForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResetPasswordForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
