import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddFavoriteModal } from './add-favorite-modal';

describe('AddFavoriteModal', () => {
  let component: AddFavoriteModal;
  let fixture: ComponentFixture<AddFavoriteModal>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddFavoriteModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddFavoriteModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
