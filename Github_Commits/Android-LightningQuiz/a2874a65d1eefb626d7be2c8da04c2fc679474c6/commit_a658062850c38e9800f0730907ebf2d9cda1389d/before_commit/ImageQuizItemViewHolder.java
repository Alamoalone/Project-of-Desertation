package com.cube.storm.ui.quiz.view.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.cube.storm.ui.QuizSettings;
import com.cube.storm.ui.model.property.ImageProperty;
import com.cube.storm.ui.model.property.TextProperty;
import com.cube.storm.ui.quiz.R;
import com.cube.storm.ui.quiz.lib.QuizEventHook;
import com.cube.storm.ui.quiz.model.quiz.ImageQuizItem;
import com.cube.storm.ui.view.ImageView;
import com.cube.storm.ui.view.TextView;
import com.cube.storm.ui.view.holder.ViewHolder;
import com.cube.storm.ui.view.holder.ViewHolderFactory;

import java.util.ArrayList;

/**
 * View holder for {@link com.cube.storm.ui.quiz.model.quiz.ImageQuizItem} in the adapter
 *
 * @author Luke Reed
 * @project Storm
 */
public class ImageQuizItemViewHolder extends ViewHolder<ImageQuizItem>
{
	public static class Factory extends ViewHolderFactory
	{
		@Override public ImageQuizItemViewHolder createViewHolder(ViewGroup parent)
		{
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_quiz_item_view, parent, false);
			return new ImageQuizItemViewHolder(view);
		}
	}

	protected TextView title;
	protected TextView hint;
	protected ViewGroup options;
	protected ArrayList<View> answerLayouts = new ArrayList<>();

	public ImageQuizItemViewHolder(View view)
	{
		super(view);

		title = (TextView)view.findViewById(R.id.title);
		hint = (TextView)view.findViewById(R.id.hint);
		options = (ViewGroup)view.findViewById(R.id.options_container);
	}

	@Override public void populateView(ImageQuizItem model)
	{
		title.populate(model.getTitle());
		hint.populate(model.getHint());

		options.removeAllViewsInLayout();

		ArrayList<TextProperty> textOptions = model.getOptions();
		ArrayList<ArrayList<ImageProperty>> images = model.getImages();
		int optionLength = textOptions.size();
		View currentRow = null;
		View currentCell = null;

		for (int index = 0; index < optionLength; index++)
		{
			if (index % 2 == 0)
			{
				currentRow = LayoutInflater.from(options.getContext()).inflate(R.layout.image_quiz_item_row, options, false);
				options.addView(currentRow);
				currentCell = currentRow.findViewById(R.id.layout1);
			}
			else
			{
				currentCell = currentRow.findViewById(R.id.layout2);
			}

			((TextView)currentCell.findViewById(R.id.label)).populate(textOptions.get(index));

			if (index < images.size())
			{
				answerLayouts.add(index, currentCell);
				ImageView imageView = currentCell.findViewById(R.id.image);
				imageView.populate(model.getImages().get(index));

				if (model.getSelectHistory().contains(index))
				{
					selectAnswer(currentCell, true);
				}
				else
				{
					// we set this in case the view is recycled
					selectAnswer(currentCell, false);
				}

				currentCell.setOnClickListener(new ModelClickListener(model, index));
				currentCell.setVisibility(View.VISIBLE);
			}
			else if (index >= images.size())
			{
				currentCell.setVisibility(View.INVISIBLE);
			}
		}

		if (optionLength % 2 != 0)
		{
			currentRow.findViewById(R.id.layout2).setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Handles the view changes when an image answer is selected or unselected
	 * @param answerLayout the layout container other views
	 * @param selectAnswer if answer is selected or unselected
	 */
	private void selectAnswer(View answerLayout, boolean selectAnswer)
	{
		// Set border around image
		View border = answerLayout.findViewById(R.id.image_border);
		if (border != null)
		{
			border.setVisibility(selectAnswer ? View.VISIBLE : View.INVISIBLE);
		}

		// Set image label style and its background
		TextView textView = answerLayout.findViewById(R.id.label);
		if (textView != null)
		{
			textView.setTextAppearance(answerLayout.getContext(),
				selectAnswer ? R.style.QuizImageLabel_Selected : R.style.QuizImageLabel_Unselected);
			textView.setBackgroundResource(selectAnswer ? R.drawable.quiz_image_label_selected_border : 0);
		}

		// Update the content description based on the selected answer and label text
		updateAnswerContentDescription(answerLayout, textView.getText(), selectAnswer);
	}
	
	/**
	 * Updates the view content description changes when an image answer is selected or unselected
	 * @param answerLayout the layout container other views
	 * @param baseText the base label text of the view
	 * @param selectAnswer if answer is selected or unselected
	 */
	protected void updateAnswerContentDescription(View answerLayout, CharSequence baseText, boolean selectAnswer)
	{
		answerLayout.setContentDescription(baseText + (selectAnswer ? " selected" : " not selected"));
	}

	private class ModelClickListener implements OnClickListener
	{
		private ImageQuizItem model;
		private int index;

		private ModelClickListener(ImageQuizItem model, int index)
		{
			this.model = model;
			this.index = index;
		}

		@Override public void onClick(View v)
		{
			if (answerLayouts != null && answerLayouts.get(index) != null)
			{
				View answerLayout = answerLayouts.get(index);

				if (!model.getSelectHistory().contains(index))
				{
					selectAnswer(answerLayout, true);
					model.getSelectHistory().add(index);
					v.announceForAccessibility(""); // reads label out loud when selected

					// select image answer
					for (QuizEventHook quizEventHook : QuizSettings.getInstance().getEventHooks())
					{
						quizEventHook.onQuizOptionSelected(v.getContext(), itemView, model, index);
					}
				}
				else
				{
					// unselect image answer
					selectAnswer(answerLayout, false);
					model.getSelectHistory().remove((Integer)index);
					v.announceForAccessibility(""); // reads label out loud when selected
				}
			}

			// unselect first selected answer if selected answers goes over answer limit
			if (model.getSelectHistory().size() > model.getLimit())
			{
				int remIndex = model.getSelectHistory().get(0);
				model.getSelectHistory().remove(0);
				View answerLayout = answerLayouts.get(remIndex);

				// unselect image answer
				selectAnswer(answerLayout, false);
			}

			// check the answers in the history
			if (model.getAnswer() != null && model.getAnswer().size() == model.getSelectHistory().size())
			{
				for (int answer : model.getAnswer())
				{
					if (model.getSelectHistory().contains(answer))
					{
						model.setCorrect(true);
					}
					else
					{
						model.setCorrect(false);
						break;
					}
				}
			}

			for (QuizEventHook quizEventHook : QuizSettings.getInstance().getEventHooks())
			{
				quizEventHook.onQuizItemAnswersChanged(v.getContext(), model);
			}
		}
	}
}
