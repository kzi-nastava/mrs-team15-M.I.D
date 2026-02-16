import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PanicNotification } from './panic-notification';

describe('PanicNotification', () => {
  let component: PanicNotification;
  let fixture: ComponentFixture<PanicNotification>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PanicNotification]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PanicNotification);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
