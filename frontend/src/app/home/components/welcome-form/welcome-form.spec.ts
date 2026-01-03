import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WelcomeForm } from './welcome-form';

describe('WelcomeForm', () => {
  let component: WelcomeForm;
  let fixture: ComponentFixture<WelcomeForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WelcomeForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WelcomeForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
