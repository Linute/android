package com.linute.linute.MainContent.Global.Articles;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.R;

import java.util.ArrayList;

/**
 * Created by mikhail on 10/25/16.
 */

public class ArticleElementAdapter extends RecyclerView.Adapter<ArticleElementAdapter.ElementVH> {

    private ArrayList<ArticleElement> elements;

    public ArticleElementAdapter(ArrayList<ArticleElement> elements) {
        this.elements = elements;
    }

    @Override
    public ElementVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType){
            case ArticleElement.ElementTypes.TEXT:
                return new TextElementVH(inflater.inflate(R.layout.article_element_text, parent, false));
            default:
                return new InvalidElementVH(inflater.inflate(R.layout.article_element_invalid, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return elements.get(position).type;
    }

    @Override
    public void onBindViewHolder(ElementVH holder, int position) {
        holder.bind(elements.get(position));
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }





    //View Holders

    static abstract class ElementVH extends RecyclerView.ViewHolder{
        ElementVH(View itemView) {super(itemView);}
        public abstract void bind(ArticleElement element);
    }

    private static class TextElementVH extends ElementVH{

        TextView vText;
        TextElementVH(View itemView) {
            super(itemView);
            vText = (TextView)itemView.findViewById(R.id.text);
        }

        @Override
        public void bind(ArticleElement element) {
            vText.setText(element.content);
        }
    }


    private static class ImageElementVH extends ElementVH{

        ImageView vImage;
        ImageElementVH(View itemView) {
            super(itemView);
            vImage = (ImageView)itemView.findViewById(R.id.image);
        }

        @Override
        public void bind(ArticleElement element) {
            Glide.with(itemView.getContext())
                    .load(element.content)
                    .into(vImage);
        }
    }

    private static class InvalidElementVH extends ElementVH{

        TextView vText;
        InvalidElementVH(View itemView) {
            super(itemView);
            vText = (TextView)itemView.findViewById(R.id.text);
        }

        @Override
        public void bind(ArticleElement element) {
            vText.setText(element.content);
        }
    }

}
