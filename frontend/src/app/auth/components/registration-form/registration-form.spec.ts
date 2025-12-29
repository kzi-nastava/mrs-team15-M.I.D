import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistrationForm } from './registration-form';

describe('RegistrationForm', () => {
  let component: RegistrationForm;
  let fixture: ComponentFixture<RegistrationForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistrationForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegistrationForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
