import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './star-rating.html',
  styleUrl: './star-rating.css',
})
export class StarRating {
  @Input() rating: number = 0;
  @Input() maxRating: number = 5;
  @Output() ratingChange = new EventEmitter<number>();

  stars: number[] = [];
  hoveredRating: number = 0;

  ngOnInit() {
    this.stars = Array(this.maxRating).fill(0).map((_, i) => i + 1);
  }

  setRating(star: number) {
    this.rating = star;
    this.ratingChange.emit(this.rating);
  }

  onStarHover(star: number) {
    this.hoveredRating = star;
  }

  onStarLeave() {
    this.hoveredRating = 0;
  }

  isStarFilled(star: number): boolean {
    const displayRating = this.hoveredRating > 0 ? this.hoveredRating : this.rating;
    return star <= displayRating;
  }
}
