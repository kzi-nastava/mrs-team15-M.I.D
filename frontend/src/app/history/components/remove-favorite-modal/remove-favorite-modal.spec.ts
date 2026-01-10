import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RemoveFavoriteModal } from './remove-favorite-modal';

describe('RemoveFavoriteModal', () => {
  let component: RemoveFavoriteModal;
  let fixture: ComponentFixture<RemoveFavoriteModal>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RemoveFavoriteModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RemoveFavoriteModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
