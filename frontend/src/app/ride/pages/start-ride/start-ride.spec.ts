import { ComponentFixture, TestBed } from '@angular/core/testing';
import {StartRide} from './start-ride';

describe('StartRide', () => {
  let component: StartRide;
  let fixture: ComponentFixture<StartRide>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StartRide]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StartRide);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

