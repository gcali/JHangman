package jhangmanclient.gui.components;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/*
 * See http://stackoverflow.com/q/1313390/1076463
 */ 

class FormatParseAllOrNothing extends Format {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Format originalFormat;

    public FormatParseAllOrNothing(Format originalFormat) {
        this.originalFormat = originalFormat;
    }

    @Override
    public StringBuffer format(
            Object obj, 
            StringBuffer toAppendTo,
            FieldPosition pos
    ) {
        System.out.println(obj);
        return this.originalFormat.format(obj, toAppendTo, pos);
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        
        int startingPosition = pos.getIndex();
        Object parsingResult = this.originalFormat.parseObject(source, pos);
        if (parsingResult != null && pos.getIndex() < source.length()) {
            //not all input could be parsed
            pos.setErrorIndex(pos.getIndex());
            pos.setIndex(startingPosition);
            return null;
        }
        return parsingResult;
    }
}
