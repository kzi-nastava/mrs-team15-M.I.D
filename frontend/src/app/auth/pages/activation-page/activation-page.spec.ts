import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivationPage } from './activation-page';

describe('ActivationPage', () => {
  let component: ActivationPage;
  let fixture: ComponentFixture<ActivationPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActivationPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActivationPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
