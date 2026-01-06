import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CurrentRide } from './current-ride';

describe('CurrentRide', () => {
  let component: CurrentRide;
  let fixture: ComponentFixture<CurrentRide>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CurrentRide]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CurrentRide);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
